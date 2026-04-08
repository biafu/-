package com.zhinengpt.campuscatering.seckill.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhinengpt.campuscatering.common.exception.BusinessException;
import com.zhinengpt.campuscatering.merchant.entity.Store;
import com.zhinengpt.campuscatering.merchant.mapper.StoreMapper;
import com.zhinengpt.campuscatering.order.entity.Order;
import com.zhinengpt.campuscatering.order.service.OrderService;
import com.zhinengpt.campuscatering.product.entity.ProductSku;
import com.zhinengpt.campuscatering.product.entity.ProductSpu;
import com.zhinengpt.campuscatering.product.mapper.ProductSkuMapper;
import com.zhinengpt.campuscatering.product.mapper.ProductSpuMapper;
import com.zhinengpt.campuscatering.seckill.config.AppSeckillProperties;
import com.zhinengpt.campuscatering.seckill.dto.SeckillActivityResponse;
import com.zhinengpt.campuscatering.seckill.dto.SeckillActivitySaveRequest;
import com.zhinengpt.campuscatering.seckill.dto.SeckillApplyRequest;
import com.zhinengpt.campuscatering.seckill.dto.SeckillApplyResponse;
import com.zhinengpt.campuscatering.seckill.dto.SeckillResultResponse;
import com.zhinengpt.campuscatering.seckill.entity.SeckillActivity;
import com.zhinengpt.campuscatering.seckill.entity.SeckillOrder;
import com.zhinengpt.campuscatering.seckill.mapper.SeckillActivityMapper;
import com.zhinengpt.campuscatering.seckill.mapper.SeckillOrderMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SeckillService {
    private static final Logger log = LoggerFactory.getLogger(SeckillService.class);

    private static final String RESULT_PENDING = "PENDING";
    private static final String RESULT_SUCCESS = "SUCCESS";
    private static final String RESULT_FAILED = "FAILED";

    private static final DefaultRedisScript<Long> PRE_DEDUCT_SCRIPT = new DefaultRedisScript<>();

    static {
        PRE_DEDUCT_SCRIPT.setResultType(Long.class);
        PRE_DEDUCT_SCRIPT.setScriptText("""
                if redis.call('exists', KEYS[2]) == 1 then
                    return 2
                end
                local stock = tonumber(redis.call('get', KEYS[1]) or '-1')
                if stock <= 0 then
                    return 1
                end
                redis.call('decr', KEYS[1])
                redis.call('set', KEYS[2], ARGV[1])
                return 0
                """);
    }

    private final SeckillActivityMapper seckillActivityMapper;
    private final SeckillOrderMapper seckillOrderMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final OrderService orderService;
    private final StoreMapper storeMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ProductSpuMapper productSpuMapper;
    private final AppSeckillProperties appSeckillProperties;
    private final ObjectMapper objectMapper;
    private final BlockingQueue<SeckillTask> taskQueue;
    private final ExecutorService consumerExecutor;

    public SeckillService(SeckillActivityMapper seckillActivityMapper,
                          SeckillOrderMapper seckillOrderMapper,
                          StringRedisTemplate stringRedisTemplate,
                          OrderService orderService,
                          StoreMapper storeMapper,
                          ProductSkuMapper productSkuMapper,
                          ProductSpuMapper productSpuMapper,
                          AppSeckillProperties appSeckillProperties,
                          ObjectMapper objectMapper) {
        this.seckillActivityMapper = seckillActivityMapper;
        this.seckillOrderMapper = seckillOrderMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.orderService = orderService;
        this.storeMapper = storeMapper;
        this.productSkuMapper = productSkuMapper;
        this.productSpuMapper = productSpuMapper;
        this.appSeckillProperties = appSeckillProperties;
        this.objectMapper = objectMapper;
        this.taskQueue = new ArrayBlockingQueue<>(appSeckillProperties.getQueueCapacity());
        this.consumerExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "seckill-consumer");
            thread.setDaemon(true);
            return thread;
        });
    }

    @PostConstruct
    public void init() {
        try {
            preloadStocks();
            log.info("Seckill stock preload finished.");
        } catch (Exception ex) {
            log.error("Seckill init preload failed. cause={}", ex.getMessage(), ex);
            if (appSeckillProperties.isFailFastOnInit()) {
                throw ex;
            }
            log.warn("Continue startup because app.seckill.fail-fast-on-init=false");
        }
        consumerExecutor.submit(this::consumeLoop);
    }

    @PreDestroy
    public void destroy() {
        consumerExecutor.shutdownNow();
    }

    public List<SeckillActivityResponse> listActivities(Long storeId) {
        return seckillActivityMapper.selectActiveByStoreId(storeId, LocalDateTime.now()).stream()
                .map(activity -> toActivityResponse(activity, currentRedisStock(activity)))
                .toList();
    }

    public List<SeckillActivityResponse> listAllActivities() {
        return seckillActivityMapper.selectAllList().stream()
                .map(activity -> toActivityResponse(activity, remainingStock(activity)))
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long saveActivity(SeckillActivitySaveRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }
        validateActivityRelation(request.getStoreId(), request.getSkuId());

        SeckillActivity activity;
        int consumed = 0;
        if (request.getId() == null) {
            activity = new SeckillActivity();
        } else {
            activity = seckillActivityMapper.selectById(request.getId());
            if (activity == null) {
                throw new BusinessException("秒杀活动不存在");
            }
            consumed = safeCount(seckillOrderMapper.countByActivityId(activity.getId()));
            if (consumed > 0
                    && (!activity.getStoreId().equals(request.getStoreId()) || !activity.getSkuId().equals(request.getSkuId()))) {
                throw new BusinessException("活动已有秒杀订单，不能修改门店或商品");
            }
        }

        activity.setStoreId(request.getStoreId());
        activity.setSkuId(request.getSkuId());
        activity.setActivityName(request.getActivityName());
        activity.setSeckillPrice(request.getSeckillPrice());
        activity.setStock(request.getStock());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setStatus(request.getStatus());

        if (request.getId() == null) {
            seckillActivityMapper.insert(activity);
        } else {
            seckillActivityMapper.updateById(activity);
        }

        syncRedisStock(activity.getId(), request.getStock(), consumed);
        return activity.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteActivity(Long activityId) {
        SeckillActivity activity = seckillActivityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException("秒杀活动不存在");
        }

        int consumed = safeCount(seckillOrderMapper.countByActivityId(activityId));
        if (consumed > 0) {
            throw new BusinessException("活动已有秒杀订单，不能删除");
        }

        seckillActivityMapper.deleteById(activityId);
        stringRedisTemplate.delete(stockKey(activityId));
    }

    public SeckillApplyResponse apply(Long userId, SeckillApplyRequest request) {
        SeckillActivity activity = requireValidActivity(request.getActivityId());
        String requestId = UUID.randomUUID().toString().replace("-", "");
        ensureStockKey(activity);

        Long result = stringRedisTemplate.execute(
                PRE_DEDUCT_SCRIPT,
                List.of(stockKey(activity.getId()), userKey(activity.getId(), userId)),
                requestId
        );
        if (result == null) {
            throw new BusinessException("秒杀服务暂不可用，请稍后再试");
        }

        if (Long.valueOf(1L).equals(result)) {
            throw new BusinessException("秒杀库存不足");
        }
        if (Long.valueOf(2L).equals(result)) {
            throw new BusinessException("当前活动仅允许一人下一单");
        }

        SeckillResultResponse pending = SeckillResultResponse.builder()
                .requestId(requestId)
                .status(RESULT_PENDING)
                .message("抢购排队中")
                .build();
        saveResult(pending);

        boolean offered = taskQueue.offer(new SeckillTask(
                requestId,
                userId,
                activity.getId(),
                request.getReceiverName(),
                request.getReceiverPhone(),
                request.getReceiverAddress(),
                request.getRemark()
        ));
        if (!offered) {
            rollbackReservation(activity.getId(), userId);
            SeckillResultResponse failed = SeckillResultResponse.builder()
                    .requestId(requestId)
                    .status(RESULT_FAILED)
                    .message("请求过多，请稍后重试")
                    .build();
            saveResult(failed);
            throw new BusinessException("秒杀排队繁忙，请稍后重试");
        }

        return SeckillApplyResponse.builder()
                .requestId(requestId)
                .status(RESULT_PENDING)
                .message("请求已受理，请稍后查询结果")
                .build();
    }

    public SeckillResultResponse queryResult(String requestId) {
        String json = stringRedisTemplate.opsForValue().get(resultKey(requestId));
        if (json == null) {
            return SeckillResultResponse.builder()
                    .requestId(requestId)
                    .status(RESULT_FAILED)
                    .message("结果已失效或请求不存在")
                    .build();
        }
        try {
            return objectMapper.readValue(json, SeckillResultResponse.class);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(5006, "秒杀结果解析失败");
        }
    }

    private void preloadStocks() {
        for (SeckillActivity activity : seckillActivityMapper.selectEnabledList()) {
            int consumed = safeCount(seckillOrderMapper.countByActivityId(activity.getId()));
            syncRedisStock(activity.getId(), activity.getStock(), consumed);
        }
    }

    private void ensureStockKey(SeckillActivity activity) {
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(stockKey(activity.getId())))) {
            return;
        }
        int consumed = safeCount(seckillOrderMapper.countByActivityId(activity.getId()));
        syncRedisStock(activity.getId(), activity.getStock(), consumed);
    }

    private int currentRedisStock(SeckillActivity activity) {
        ensureStockKey(activity);
        String stock = stringRedisTemplate.opsForValue().get(stockKey(activity.getId()));
        return stock == null ? 0 : Integer.parseInt(stock);
    }

    private int remainingStock(SeckillActivity activity) {
        int consumed = safeCount(seckillOrderMapper.countByActivityId(activity.getId()));
        return Math.max(safeCount(activity.getStock()) - consumed, 0);
    }

    private SeckillActivityResponse toActivityResponse(SeckillActivity activity, int stock) {
        return SeckillActivityResponse.builder()
                .id(activity.getId())
                .storeId(activity.getStoreId())
                .skuId(activity.getSkuId())
                .activityName(activity.getActivityName())
                .seckillPrice(activity.getSeckillPrice())
                .stock(stock)
                .totalStock(activity.getStock())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .status(activity.getStatus())
                .createdAt(activity.getCreatedAt())
                .updatedAt(activity.getUpdatedAt())
                .build();
    }

    private void consumeLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                SeckillTask task = taskQueue.take();
                processTask(task);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processTask(SeckillTask task) {
        try {
            SeckillOrder existed = seckillOrderMapper.selectByUserAndActivity(task.userId(), task.activityId());
            if (existed != null) {
                saveResult(SeckillResultResponse.builder()
                        .requestId(task.requestId())
                        .status(RESULT_SUCCESS)
                        .message("秒杀订单已存在")
                        .orderId(existed.getOrderId())
                        .orderNo(existed.getOrderNo())
                        .build());
                return;
            }

            SeckillActivity activity = requireValidActivity(task.activityId());
            Order order = orderService.createSeckillOrder(
                    task.userId(),
                    activity,
                    task.receiverName(),
                    task.receiverPhone(),
                    task.receiverAddress(),
                    task.remark()
            );

            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setActivityId(activity.getId());
            seckillOrder.setUserId(task.userId());
            seckillOrder.setOrderId(order.getId());
            seckillOrder.setOrderNo(order.getOrderNo());
            seckillOrder.setStatus(0);
            seckillOrderMapper.insert(seckillOrder);

            saveResult(SeckillResultResponse.builder()
                    .requestId(task.requestId())
                    .status(RESULT_SUCCESS)
                    .message("秒杀成功，请尽快支付")
                    .orderId(order.getId())
                    .orderNo(order.getOrderNo())
                    .build());
        } catch (DuplicateKeyException ex) {
            SeckillOrder existed = seckillOrderMapper.selectByUserAndActivity(task.userId(), task.activityId());
            if (existed != null) {
                saveResult(SeckillResultResponse.builder()
                        .requestId(task.requestId())
                        .status(RESULT_SUCCESS)
                        .message("秒杀订单已存在")
                        .orderId(existed.getOrderId())
                        .orderNo(existed.getOrderNo())
                        .build());
                return;
            }
            rollbackReservation(task.activityId(), task.userId());
            saveResult(SeckillResultResponse.builder()
                    .requestId(task.requestId())
                    .status(RESULT_FAILED)
                    .message("重复秒杀请求")
                    .build());
        } catch (Exception ex) {
            rollbackReservation(task.activityId(), task.userId());
            saveResult(SeckillResultResponse.builder()
                    .requestId(task.requestId())
                    .status(RESULT_FAILED)
                    .message(ex.getMessage() == null ? "秒杀失败" : ex.getMessage())
                    .build());
        }
    }

    private SeckillActivity requireValidActivity(Long activityId) {
        SeckillActivity activity = seckillActivityMapper.selectById(activityId);
        if (activity == null || activity.getStatus() == null || activity.getStatus() != 1) {
            throw new BusinessException("秒杀活动不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        if (activity.getStartTime().isAfter(now)) {
            throw new BusinessException("秒杀活动尚未开始");
        }
        if (activity.getEndTime().isBefore(now)) {
            throw new BusinessException("秒杀活动已结束");
        }
        return activity;
    }

    private void validateActivityRelation(Long storeId, Long skuId) {
        Store store = storeMapper.selectById(storeId);
        if (store == null || (store.getDeleted() != null && store.getDeleted() == 1)) {
            throw new BusinessException("门店不存在");
        }
        ProductSku sku = productSkuMapper.selectById(skuId);
        if (sku == null) {
            throw new BusinessException("商品规格不存在");
        }
        ProductSpu spu = productSpuMapper.selectById(sku.getSpuId());
        if (spu == null || !storeId.equals(spu.getStoreId())) {
            throw new BusinessException("秒杀商品与门店不匹配");
        }
        if (spu.getSaleStatus() == null || spu.getSaleStatus() != 1 || sku.getStatus() == null || sku.getStatus() != 1) {
            throw new BusinessException("秒杀商品必须处于上架状态");
        }
    }

    private void syncRedisStock(Long activityId, Integer totalStock, int consumed) {
        int remaining = totalStock - consumed;
        if (remaining < 0) {
            throw new BusinessException("活动库存不能小于已秒杀数量");
        }
        stringRedisTemplate.opsForValue().set(stockKey(activityId), String.valueOf(remaining));
    }

    private int safeCount(Integer count) {
        return count == null ? 0 : count;
    }

    private void rollbackReservation(Long activityId, Long userId) {
        stringRedisTemplate.opsForValue().increment(stockKey(activityId));
        stringRedisTemplate.delete(userKey(activityId, userId));
    }

    private void saveResult(SeckillResultResponse resultResponse) {
        try {
            stringRedisTemplate.opsForValue().set(
                    resultKey(resultResponse.getRequestId()),
                    objectMapper.writeValueAsString(resultResponse),
                    Duration.ofMinutes(appSeckillProperties.getResultTtlMinutes())
            );
        } catch (JsonProcessingException ex) {
            throw new BusinessException(5005, "秒杀结果保存失败");
        }
    }

    private String stockKey(Long activityId) {
        return "seckill:stock:" + activityId;
    }

    private String userKey(Long activityId, Long userId) {
        return "seckill:ordered:" + activityId + ":" + userId;
    }

    private String resultKey(String requestId) {
        return "seckill:result:" + requestId;
    }

    private record SeckillTask(String requestId,
                               Long userId,
                               Long activityId,
                               String receiverName,
                               String receiverPhone,
                               String receiverAddress,
                               String remark) {
    }
}
