package com.zhinengpt.campuscatering.order.service;

import com.zhinengpt.campuscatering.auth.mapper.DeliveryUserMapper;
import com.zhinengpt.campuscatering.cart.service.CartService;
import com.zhinengpt.campuscatering.common.enums.OrderStatus;
import com.zhinengpt.campuscatering.common.exception.BusinessException;
import com.zhinengpt.campuscatering.common.util.RedisIdGenerator;
import com.zhinengpt.campuscatering.message.service.StudentMessageService;
import com.zhinengpt.campuscatering.message.OrderNotifier;
import com.zhinengpt.campuscatering.merchant.entity.Store;
import com.zhinengpt.campuscatering.merchant.service.MerchantService;
import com.zhinengpt.campuscatering.merchant.mapper.StoreMapper;
import com.zhinengpt.campuscatering.order.config.AppOrderProperties;
import com.zhinengpt.campuscatering.order.dto.DispatchRequest;
import com.zhinengpt.campuscatering.order.dto.DeliveryOrderResponse;
import com.zhinengpt.campuscatering.order.dto.OrderCreateResponse;
import com.zhinengpt.campuscatering.order.dto.OrderDetailResponse;
import com.zhinengpt.campuscatering.order.dto.OrderExceptionHandleRequest;
import com.zhinengpt.campuscatering.order.dto.OrderExceptionResolveRequest;
import com.zhinengpt.campuscatering.order.dto.OrderExceptionResponse;
import com.zhinengpt.campuscatering.order.dto.OrderItemRequest;
import com.zhinengpt.campuscatering.order.dto.OrderItemVO;
import com.zhinengpt.campuscatering.order.dto.OrderPreviewRequest;
import com.zhinengpt.campuscatering.order.dto.OrderPreviewResponse;
import com.zhinengpt.campuscatering.order.dto.OrderPreviewSku;
import com.zhinengpt.campuscatering.order.dto.OrderWsMessage;
import com.zhinengpt.campuscatering.order.entity.DeliveryOrder;
import com.zhinengpt.campuscatering.order.entity.OrderException;
import com.zhinengpt.campuscatering.order.entity.Order;
import com.zhinengpt.campuscatering.order.entity.OrderItem;
import com.zhinengpt.campuscatering.order.mapper.DeliveryOrderMapper;
import com.zhinengpt.campuscatering.order.mapper.OrderExceptionMapper;
import com.zhinengpt.campuscatering.order.mapper.OrderItemMapper;
import com.zhinengpt.campuscatering.order.mapper.OrderMapper;
import com.zhinengpt.campuscatering.product.entity.ComboItem;
import com.zhinengpt.campuscatering.product.entity.ProductSku;
import com.zhinengpt.campuscatering.product.entity.ProductSpu;
import com.zhinengpt.campuscatering.product.mapper.ProductSkuMapper;
import com.zhinengpt.campuscatering.product.service.ProductService;
import com.zhinengpt.campuscatering.promotion.service.CouponService;
import com.zhinengpt.campuscatering.seckill.entity.SeckillActivity;
import com.zhinengpt.campuscatering.seckill.entity.SeckillOrder;
import com.zhinengpt.campuscatering.seckill.mapper.SeckillOrderMapper;
import com.zhinengpt.campuscatering.statistics.dto.MerchantHotProductResponse;
import com.zhinengpt.campuscatering.statistics.dto.MerchantOverviewResponse;
import com.zhinengpt.campuscatering.statistics.dto.MerchantRankResponse;
import com.zhinengpt.campuscatering.statistics.dto.MerchantStatusDistributionResponse;
import com.zhinengpt.campuscatering.statistics.dto.MerchantTrendPointResponse;
import com.zhinengpt.campuscatering.trace.dto.OrderLogResponse;
import com.zhinengpt.campuscatering.trace.service.OrderTraceService;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private static final String ALERT_KEY_MERCHANT_ACCEPT_TIMEOUT = "order:alert:merchant-timeout:";
    private static final String ALERT_KEY_DELIVERY_TIMEOUT = "order:alert:delivery-timeout:";
    private static final String URGE_KEY_PREFIX = "order:urge:";
    private static final long UNASSIGNED_DELIVERY_USER_ID = 0L;

    private final StoreMapper storeMapper;
    private final MerchantService merchantService;
    private final ProductService productService;
    private final ProductSkuMapper productSkuMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final DeliveryOrderMapper deliveryOrderMapper;
    private final DeliveryUserMapper deliveryUserMapper;
    private final OrderExceptionMapper orderExceptionMapper;
    private final RedisIdGenerator redisIdGenerator;
    private final CartService cartService;
    private final OrderNotifier orderNotifier;
    private final StudentMessageService studentMessageService;
    private final AppOrderProperties appOrderProperties;
    private final SeckillOrderMapper seckillOrderMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final OrderTraceService orderTraceService;
    private final CouponService couponService;

    public OrderService(StoreMapper storeMapper,
                        MerchantService merchantService,
                        ProductService productService,
                        ProductSkuMapper productSkuMapper,
                        OrderMapper orderMapper,
                        OrderItemMapper orderItemMapper,
                        DeliveryOrderMapper deliveryOrderMapper,
                        DeliveryUserMapper deliveryUserMapper,
                        OrderExceptionMapper orderExceptionMapper,
                        RedisIdGenerator redisIdGenerator,
                        CartService cartService,
                        OrderNotifier orderNotifier,
                        StudentMessageService studentMessageService,
                        AppOrderProperties appOrderProperties,
                        SeckillOrderMapper seckillOrderMapper,
                        StringRedisTemplate stringRedisTemplate,
                        OrderTraceService orderTraceService,
                        CouponService couponService) {
        this.storeMapper = storeMapper;
        this.merchantService = merchantService;
        this.productService = productService;
        this.productSkuMapper = productSkuMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.deliveryOrderMapper = deliveryOrderMapper;
        this.deliveryUserMapper = deliveryUserMapper;
        this.orderExceptionMapper = orderExceptionMapper;
        this.redisIdGenerator = redisIdGenerator;
        this.cartService = cartService;
        this.orderNotifier = orderNotifier;
        this.studentMessageService = studentMessageService;
        this.appOrderProperties = appOrderProperties;
        this.seckillOrderMapper = seckillOrderMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.orderTraceService = orderTraceService;
        this.couponService = couponService;
    }

    public OrderPreviewResponse preview(Long userId, OrderPreviewRequest request) {
        TradeContext tradeContext = buildTradeContext(userId, request, false);
        return buildPreviewResponse(tradeContext);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderCreateResponse create(Long userId, OrderPreviewRequest request) {
        TradeContext tradeContext = buildTradeContext(userId, request, true);

        Order order = new Order();
        order.setOrderNo(redisIdGenerator.nextId("order"));
        order.setUserId(userId);
        order.setStoreId(tradeContext.store().getId());
        order.setMerchantId(tradeContext.store().getMerchantId());
        order.setOrderType(1);
        order.setOrderStatus(OrderStatus.PENDING_PAYMENT.name());
        order.setGoodsAmount(tradeContext.goodsAmount());
        order.setDeliveryFee(tradeContext.deliveryFee());
        order.setDiscountAmount(tradeContext.discountAmount());
        order.setPayAmount(tradeContext.payAmount());
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setReceiverAddress(request.getReceiverAddress());
        order.setRemark(request.getRemark());
        orderMapper.insert(order);
        couponService.useCoupon(userId, tradeContext.userCouponId(), order.getId());
        orderTraceService.initPendingPayment(order, "MOCK");

        for (StockDeductItem deductItem : tradeContext.stockDeductItems()) {
            ProductSku latestSku = productService.getSkuByIdFresh(deductItem.skuId());
            int updated = productSkuMapper.deductStock(deductItem.skuId(), deductItem.quantity(), latestSku.getVersion());
            if (updated == 0) {
                throw new BusinessException("库存不足，请刷新后重试");
            }
            productService.evictSkuCache(deductItem.skuId());
        }

        for (TradeItem item : tradeContext.items()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setSkuId(item.sku().getId());
            orderItem.setSpuName(item.spu().getProductName());
            orderItem.setSkuName(item.sku().getSkuName());
            orderItem.setPrice(item.sku().getPrice());
            orderItem.setQuantity(item.quantity());
            orderItem.setTotalAmount(item.totalAmount());
            orderItemMapper.insert(orderItem);
        }

        cartService.clearStoreCart(userId, request.getStoreId());
        orderTraceService.log(order.getId(), order.getOrderStatus(), "CREATE", "STUDENT", userId, "学生提交普通订单");
        notifyStudent(order, "订单创建成功", "订单已创建，请尽快完成支付");
        return OrderCreateResponse.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .orderStatus(order.getOrderStatus())
                .payAmount(order.getPayAmount())
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public void pay(Long userId, Long orderId) {
        Order order = getOwnedOrder(userId, orderId);
        if (orderMapper.pay(order.getId()) == 0) {
            throw new BusinessException("订单状态不允许支付");
        }
        Order latest = orderMapper.selectById(orderId);
        orderTraceService.markPaymentSuccess(latest, "MOCK", "MOCK-" + latest.getOrderNo());
        orderTraceService.log(latest.getId(), latest.getOrderStatus(), "PAY", "STUDENT", userId, "用户完成支付");
        if (latest.getOrderType() != null && latest.getOrderType() == 2) {
            seckillOrderMapper.updateStatusByOrderId(latest.getId(), 1);
        }
        notifyMerchant("NEW_ORDER", latest, "您有新的已支付订单");
        notifyStudent(latest, "支付成功", "订单支付成功，商家将尽快接单");
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelByStudent(Long userId, Long orderId) {
        Order order = getOwnedOrder(userId, orderId);
        if (orderMapper.cancel(order.getId(), OrderStatus.PENDING_PAYMENT.name(), "用户取消") == 0) {
            throw new BusinessException("当前订单不可取消");
        }
        restoreStock(order.getId());
        releaseSeckillReservation(order);
        couponService.releaseByOrderId(order.getId());
        Order latest = orderMapper.selectById(orderId);
        orderTraceService.log(latest.getId(), latest.getOrderStatus(), "CANCEL", "STUDENT", userId, "用户取消未支付订单");
        notifyMerchant("ORDER_CANCELLED", latest, "订单已取消");
        notifyStudent(latest, "订单已取消", "你已取消订单，库存和优惠券已自动回滚");
    }

    /*
    @Transactional(rollbackFor = Exception.class)
    public void requestRefund(Long userId, Long orderId) {
        Order order = getOwnedOrder(userId, orderId);
        if (orderMapper.refund(order.getId(), "鐢ㄦ埛鐢宠閫€娆?) == 0) {
            throw new BusinessException("褰撳墠璁㈠崟鏆備笉鏀寔閫€娆?");
        }
        restoreStock(order.getId());
        releaseSeckillReservation(order);
        couponService.releaseByOrderId(order.getId());
        Order latest = orderMapper.selectById(orderId);
        orderTraceService.markRefundSuccess(latest, "REFUND-" + latest.getOrderNo());
        orderTraceService.log(latest.getId(), latest.getOrderStatus(), "REFUND", "STUDENT", userId, "鐢ㄦ埛鐢宠閫€娆惧苟宸插畬鎴愬師璺€€鍥?");
        notifyMerchant("ORDER_REFUNDED", latest, "璁㈠崟宸插畬鎴愰€€娆?");
        notifyStudent(latest, "閫€娆炬垚鍔?", "璁㈠崟閫€娆惧凡鍘熻矾閫€鍥烇紝璇锋敞鎰忔煡鏀?");
    }

    */

    @Transactional(rollbackFor = Exception.class)
    public void requestRefund(Long userId, Long orderId) {
        Order order = getOwnedOrder(userId, orderId);
        if (orderMapper.refund(order.getId(), "\u7528\u6237\u7533\u8bf7\u9000\u6b3e") == 0) {
            throw new BusinessException("\u5f53\u524d\u8ba2\u5355\u6682\u4e0d\u652f\u6301\u9000\u6b3e");
        }
        restoreStock(order.getId());
        releaseSeckillReservation(order);
        couponService.releaseByOrderId(order.getId());
        Order latest = orderMapper.selectById(orderId);
        orderTraceService.markRefundSuccess(latest, "REFUND-" + latest.getOrderNo());
        orderTraceService.log(
                latest.getId(),
                latest.getOrderStatus(),
                "REFUND",
                "STUDENT",
                userId,
                "\u7528\u6237\u7533\u8bf7\u9000\u6b3e\u5e76\u5df2\u5b8c\u6210\u539f\u8def\u9000\u56de"
        );
        notifyMerchant("ORDER_REFUNDED", latest, "\u8ba2\u5355\u5df2\u5b8c\u6210\u9000\u6b3e");
        notifyStudent(
                latest,
                "\u9000\u6b3e\u6210\u529f",
                "\u8ba2\u5355\u9000\u6b3e\u5df2\u539f\u8def\u9000\u56de\uff0c\u8bf7\u6ce8\u610f\u67e5\u6536"
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderCreateResponse reorder(Long userId, Long sourceOrderId) {
        Order sourceOrder = getOwnedOrder(userId, sourceOrderId);
        List<OrderItem> sourceItems = orderItemMapper.selectByOrderId(sourceOrder.getId());
        if (sourceItems.isEmpty()) {
            throw new BusinessException("原订单无可复购商品");
        }
        OrderPreviewRequest request = new OrderPreviewRequest();
        request.setStoreId(sourceOrder.getStoreId());
        request.setReceiverName(sourceOrder.getReceiverName());
        request.setReceiverPhone(sourceOrder.getReceiverPhone());
        request.setReceiverAddress(sourceOrder.getReceiverAddress());
        request.setRemark(sourceOrder.getRemark());
        request.setItems(sourceItems.stream().map(item -> {
            OrderItemRequest itemRequest = new OrderItemRequest();
            itemRequest.setSkuId(item.getSkuId());
            itemRequest.setQuantity(item.getQuantity());
            return itemRequest;
        }).toList());
        return create(userId, request);
    }

    public void urge(Long userId, Long orderId) {
        Order order = getOwnedOrder(userId, orderId);
        if (!List.of(
                OrderStatus.PAID.name(),
                OrderStatus.ACCEPTED.name(),
                OrderStatus.PREPARING.name(),
                OrderStatus.WAITING_DELIVERY.name(),
                OrderStatus.DELIVERING.name()
        ).contains(order.getOrderStatus())) {
            throw new BusinessException("当前订单状态不支持催单");
        }
        String key = URGE_KEY_PREFIX + orderId + ":" + userId;
        boolean allowed = Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(
                key,
                "1",
                Duration.ofSeconds(appOrderProperties.getUrgeCooldownSeconds())
        ));
        if (!allowed) {
            throw new BusinessException("催单过于频繁，请稍后再试");
        }
        orderTraceService.log(order.getId(), order.getOrderStatus(), "URGE", "STUDENT", userId, "学生发起催单");
        notifyMerchant("ORDER_URGED", order, "用户发起了催单，请优先处理");
        notifyStudent(order, "催单已发送", "已提醒商家加急处理你的订单");
    }

    public List<OrderDetailResponse> listStudentOrders(Long userId) {
        return orderMapper.selectByUserId(userId).stream().map(this::toDetail).toList();
    }

    public OrderDetailResponse studentOrderDetail(Long userId, Long orderId) {
        return toDetail(getOwnedOrder(userId, orderId));
    }

    public List<OrderLogResponse> studentOrderLogs(Long userId, Long orderId) {
        return orderTraceService.listLogs(getOwnedOrder(userId, orderId).getId());
    }

    public List<OrderDetailResponse> listMerchantOrders(Long merchantId) {
        return orderMapper.selectByMerchantId(merchantId).stream().map(this::toDetail).toList();
    }

    public List<OrderLogResponse> merchantOrderLogs(Long merchantId, Long orderId) {
        return orderTraceService.listLogs(getMerchantOwnedOrder(merchantId, orderId).getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void accept(Long merchantId, Long orderId) {
        Order order = getMerchantOwnedOrder(merchantId, orderId);
        if (orderMapper.accept(order.getId()) == 0) {
            throw new BusinessException("当前订单不可接单");
        }
        Order latest = orderMapper.selectById(orderId);
        orderTraceService.log(latest.getId(), latest.getOrderStatus(), "ACCEPT", "MERCHANT", merchantId, "商户已接单");
        notifyMerchant("ORDER_ACCEPTED", latest, "订单已接单");
        notifyStudent(latest, "商家已接单", "你的订单已由商家接单并开始处理");
    }

    @Transactional(rollbackFor = Exception.class)
    public void reject(Long merchantId, Long orderId) {
        Order order = getMerchantOwnedOrder(merchantId, orderId);
        if (orderMapper.cancel(order.getId(), OrderStatus.PAID.name(), "商家拒单") == 0) {
            throw new BusinessException("当前订单不可拒单");
        }
        restoreStock(order.getId());
        releaseSeckillReservation(order);
        couponService.releaseByOrderId(order.getId());
        Order latest = orderMapper.selectById(orderId);
        orderTraceService.log(latest.getId(), latest.getOrderStatus(), "REJECT", "MERCHANT", merchantId, "商户拒绝接单");
        notifyMerchant("ORDER_REJECTED", latest, "订单已被商家拒绝");
        notifyStudent(latest, "订单被拒绝", "商家已拒单，系统已自动回滚库存与优惠券");
    }

    @Transactional(rollbackFor = Exception.class)
    public void prepare(Long merchantId, Long orderId) {
        Order order = getMerchantOwnedOrder(merchantId, orderId);
        if (orderMapper.prepare(order.getId()) == 0) {
            throw new BusinessException("当前订单不可备餐");
        }
        Order latest = orderMapper.selectById(orderId);
        orderTraceService.log(latest.getId(), latest.getOrderStatus(), "PREPARE", "MERCHANT", merchantId, "商户开始备餐");
        notifyMerchant("ORDER_PREPARING", latest, "订单备餐中");
        notifyStudent(latest, "订单备餐中", "商家正在为你准备餐品");
    }

    @Transactional(rollbackFor = Exception.class)
    public void finishPrepare(Long merchantId, Long orderId) {
        Order order = getMerchantOwnedOrder(merchantId, orderId);
        ensureDeliveryPoolOrder(order.getId());
        if (orderMapper.finishPrepare(order.getId()) == 0) {
            throw new BusinessException("当前订单不可出餐");
        }
        Order latest = orderMapper.selectById(orderId);
        orderTraceService.log(latest.getId(), latest.getOrderStatus(), "FINISH_PREPARE", "MERCHANT", merchantId, "商户出餐完成，等待配送");
        notifyMerchant("ORDER_WAITING_DELIVERY", latest, "订单待配送");
        notifyStudent(latest, "餐品已出餐", "商家已完成出餐，等待配送员取餐");
    }

    @Transactional(rollbackFor = Exception.class)
    public void dispatch(DispatchRequest request) {
        Order order = getOrderOrThrow(request.getOrderId());
        if (deliveryUserMapper.selectById(request.getDeliveryUserId()) == null) {
            throw new BusinessException("配送员不存在");
        }
        if (orderMapper.delivering(order.getId()) == 0) {
            throw new BusinessException("当前订单不可调度配送");
        }
        DeliveryOrder deliveryOrder = deliveryOrderMapper.selectByOrderId(order.getId());
        if (deliveryOrder == null) {
            deliveryOrder = new DeliveryOrder();
            deliveryOrder.setOrderId(order.getId());
            deliveryOrder.setDeliveryUserId(request.getDeliveryUserId());
            deliveryOrder.setDispatchStatus(1);
            deliveryOrder.setDispatchRemark(request.getDispatchRemark());
            deliveryOrderMapper.insert(deliveryOrder);
        } else if (deliveryOrderMapper.assign(order.getId(), request.getDeliveryUserId(), request.getDispatchRemark()) == 0) {
            throw new BusinessException("褰撳墠璁㈠崟宸茶鍏朵粬閰嶉€佸憳鎺ュ崟");
        }
        Order latest = orderMapper.selectById(order.getId());
        orderTraceService.log(latest.getId(), latest.getOrderStatus(), "DISPATCH", "ADMIN", null, "平台已分配配送员");
        notifyMerchant("ORDER_DELIVERING", latest, "订单配送中");
        notifyStudent(latest, "订单配送中", "配送员已接单，正在为你配送");
    }

    @Transactional(rollbackFor = Exception.class)
    public void completeDelivery(Long orderId) {
        Order order = getOrderOrThrow(orderId);
        if (orderMapper.complete(order.getId()) == 0) {
            throw new BusinessException("当前订单不可完成配送");
        }
        deliveryOrderMapper.complete(orderId);
        Order latest = orderMapper.selectById(order.getId());
        orderTraceService.log(latest.getId(), latest.getOrderStatus(), "COMPLETE", "DELIVERY", null, "订单配送完成");
        notifyMerchant("ORDER_COMPLETED", latest, "订单已完成");
        notifyStudent(latest, "订单已完成", "订单已送达，欢迎评价本次服务");
    }

    public List<DeliveryOrderResponse> listDeliveryOrders(Long deliveryUserId) {
        return deliveryOrderMapper.selectByDeliveryUserId(deliveryUserId).stream()
                .map(this::toDeliveryResponse)
                .toList();
    }

    public List<DeliveryOrderResponse> listAvailableDeliveryOrders() {
        return deliveryOrderMapper.selectAvailableOrders().stream()
                .map(this::toDeliveryResponse)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void claimDeliveryOrder(Long deliveryUserId, Long orderId) {
        Order order = getOrderOrThrow(orderId);
        if (orderMapper.delivering(order.getId()) == 0) {
            throw new BusinessException("褰撳墠璁㈠崟宸茶鍏朵粬閰嶉€佸憳鎺ュ崟");
        }
        if (deliveryOrderMapper.claim(orderId, deliveryUserId) == 0) {
            throw new BusinessException("褰撳墠璁㈠崟宸茶鍏朵粬閰嶉€佸憳鎺ュ崟");
        }
        Order latest = orderMapper.selectById(order.getId());
        orderTraceService.log(latest.getId(), latest.getOrderStatus(), "CLAIM", "DELIVERY", deliveryUserId, "閰嶉€佸憳鎶㈠崟鎴愬姛");
        notifyMerchant("ORDER_DELIVERING", latest, "閰嶉€佸憳宸叉姠鍗曪紝璁㈠崟閰嶉€佷腑");
        notifyStudent(latest, "璁㈠崟閰嶉€佷腑", "閰嶉€佸憳宸叉帴鍗曪紝姝ｅ湪涓轰綘閰嶉€?");
    }

    @Transactional(rollbackFor = Exception.class)
    public void pickupDeliveryOrder(Long deliveryUserId, Long orderId) {
        if (deliveryOrderMapper.pickup(orderId, deliveryUserId) == 0) {
            throw new BusinessException("当前订单不可标记为已取餐");
        }
        Order order = getOrderOrThrow(orderId);
        orderTraceService.log(order.getId(), order.getOrderStatus(), "PICKUP", "DELIVERY", deliveryUserId, "配送员已取餐");
        notifyMerchant("ORDER_PICKED_UP", order, "配送员已取餐，订单正在配送中");
        notifyStudent(order, "骑手已取餐", "骑手已从商家取餐，正在为你送达");
    }

    @Transactional(rollbackFor = Exception.class)
    public void completeDeliveryByUser(Long deliveryUserId, Long orderId) {
        if (deliveryOrderMapper.completeByDeliveryUser(orderId, deliveryUserId) == 0) {
            throw new BusinessException("当前订单不可由该配送员完成");
        }
        Order order = getOrderOrThrow(orderId);
        if (orderMapper.complete(order.getId()) == 0) {
            throw new BusinessException("当前订单不可完成配送");
        }
        Order latest = orderMapper.selectById(order.getId());
        orderTraceService.log(latest.getId(), latest.getOrderStatus(), "COMPLETE", "DELIVERY", deliveryUserId, "配送员确认订单送达");
        notifyMerchant("ORDER_COMPLETED", latest, "订单已完成");
        notifyStudent(latest, "订单已完成", "订单已送达，欢迎评价本次服务");
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelTimeoutOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(appOrderProperties.getUnpaidTimeoutMinutes());
        for (Order order : orderMapper.selectTimeoutPendingOrders(cutoff)) {
            if (orderMapper.cancel(order.getId(), OrderStatus.PENDING_PAYMENT.name(), "支付超时自动取消") > 0) {
                restoreStock(order.getId());
                releaseSeckillReservation(order);
                couponService.releaseByOrderId(order.getId());
                Order latest = orderMapper.selectById(order.getId());
                orderTraceService.log(latest.getId(), latest.getOrderStatus(), "TIMEOUT_CANCEL", "SYSTEM", null, "订单支付超时，系统自动取消");
                notifyStudent(latest, "订单超时取消", "订单因超时未支付已自动取消");
            }
        }
    }

    public void merchantTimeoutAlert() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(appOrderProperties.getMerchantAcceptTimeoutMinutes());
        for (Order order : orderMapper.selectTimeoutPaidOrders(cutoff)) {
            if (!markAlertOnce(ALERT_KEY_MERCHANT_ACCEPT_TIMEOUT + order.getId())) {
                continue;
            }
            orderTraceService.log(order.getId(), order.getOrderStatus(), "MERCHANT_TIMEOUT_ALERT", "SYSTEM", null, "订单支付后商户超时未接单，请尽快处理");
            notifyMerchant("MERCHANT_TIMEOUT_ALERT", order, "订单支付后超时未接单，请尽快处理");
            notifyStudent(order, "订单处理稍有延迟", "商家暂未接单，平台已自动提醒商家尽快处理");
        }
    }

    public void deliveryTimeoutAlert() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(appOrderProperties.getDeliveryTimeoutMinutes());
        for (Order order : orderMapper.selectTimeoutDeliveringOrders(cutoff)) {
            if (!markAlertOnce(ALERT_KEY_DELIVERY_TIMEOUT + order.getId())) {
                continue;
            }
            orderTraceService.log(order.getId(), order.getOrderStatus(), "DELIVERY_TIMEOUT_ALERT", "SYSTEM", null, "订单配送超时，请人工介入处理");
            notifyMerchant("DELIVERY_TIMEOUT_ALERT", order, "订单配送超时，请尽快跟进");
            notifyStudent(order, "配送有延迟", "当前订单配送超时，平台正在加急处理");
        }
    }

    public MerchantOverviewResponse merchantOverview(Long merchantId) {
        return MerchantOverviewResponse.builder()
                .todayOrderCount(orderMapper.countMerchantTodayOrders(merchantId))
                .totalOrderCount(orderMapper.countMerchantTotalOrders(merchantId))
                .todayRevenue(orderMapper.sumMerchantTodayRevenue(merchantId))
                .build();
    }

    public Long countPlatformOrders() {
        return orderMapper.countPlatformOrders();
    }

    public BigDecimal sumPlatformRevenue() {
        return orderMapper.sumPlatformRevenue();
    }

    public List<MerchantRankResponse> merchantRank(int days, int limit) {
        int safeDays = Math.max(1, Math.min(days, 90));
        int safeLimit = Math.max(1, Math.min(limit, 50));
        LocalDateTime startTime = LocalDate.now().minusDays(safeDays - 1L).atStartOfDay();
        return orderMapper.selectMerchantRank(startTime, safeLimit);
    }

    public List<MerchantTrendPointResponse> merchantTrend(Long merchantId, int days) {
        int safeDays = Math.max(1, Math.min(days, 90));
        LocalDate startDate = LocalDate.now().minusDays(safeDays - 1L);
        return orderMapper.selectMerchantTrend(merchantId, startDate);
    }

    public List<MerchantStatusDistributionResponse> merchantStatusDistribution(Long merchantId, int days) {
        int safeDays = Math.max(1, Math.min(days, 90));
        LocalDateTime startTime = LocalDate.now().minusDays(safeDays - 1L).atStartOfDay();
        return orderMapper.selectMerchantStatusDistribution(merchantId, startTime);
    }

    public List<MerchantHotProductResponse> merchantHotProducts(Long merchantId, int days, int limit) {
        int safeDays = Math.max(1, Math.min(days, 90));
        int safeLimit = Math.max(1, Math.min(limit, 50));
        LocalDateTime startTime = LocalDate.now().minusDays(safeDays - 1L).atStartOfDay();
        return orderItemMapper.selectMerchantHotProducts(merchantId, startTime, safeLimit);
    }

    @Transactional(rollbackFor = Exception.class)
    public void reportException(Long merchantId, OrderExceptionHandleRequest request) {
        Order order = getMerchantOwnedOrder(merchantId, request.getOrderId());
        if (OrderStatus.COMPLETED.name().equals(order.getOrderStatus()) || OrderStatus.CANCELLED.name().equals(order.getOrderStatus())) {
            throw new BusinessException("已完成或已取消订单不允许上报异常");
        }
        OrderException existed = orderExceptionMapper.selectByOrderId(order.getId());
        if (existed == null) {
            OrderException exception = new OrderException();
            exception.setOrderId(order.getId());
            exception.setMerchantId(merchantId);
            exception.setStatus("OPEN");
            exception.setReason(request.getReason().trim());
            orderExceptionMapper.insert(exception);
        } else {
            existed.setStatus("OPEN");
            existed.setReason(request.getReason().trim());
            existed.setResolvedRemark(null);
            orderExceptionMapper.updateByOrderId(existed);
        }
        orderTraceService.log(order.getId(), order.getOrderStatus(), "EXCEPTION_REPORT", "MERCHANT", merchantId, "商户上报异常订单：" + request.getReason().trim());
        notifyStudent(order, "订单异常处理中", "商家已上报订单异常，正在加急处理");
    }

    @Transactional(rollbackFor = Exception.class)
    public void resolveException(Long merchantId, OrderExceptionResolveRequest request) {
        Order order = getMerchantOwnedOrder(merchantId, request.getOrderId());
        if (orderExceptionMapper.resolve(order.getId(), merchantId, request.getResolvedRemark().trim()) == 0) {
            throw new BusinessException("异常单不存在或无权限处理");
        }
        orderTraceService.log(order.getId(), order.getOrderStatus(), "EXCEPTION_RESOLVE", "MERCHANT", merchantId, "商户完成异常单处理");
        notifyStudent(order, "订单异常已处理", "商家已处理订单异常，请查看最新订单状态");
    }

    public List<OrderExceptionResponse> listExceptions(Long merchantId, String status) {
        String safeStatus = StringUtils.hasText(status) ? status.trim() : null;
        return orderExceptionMapper.selectByMerchant(merchantId, safeStatus);
    }

    @Transactional(rollbackFor = Exception.class)
    public Order createSeckillOrder(Long userId,
                                    SeckillActivity activity,
                                    String receiverName,
                                    String receiverPhone,
                                    String receiverAddress,
                                    String remark) {
        if (seckillOrderMapper.selectByUserAndActivity(userId, activity.getId()) != null) {
            throw new BusinessException("当前活动仅允许一人下一单");
        }

        Store store = storeMapper.selectById(activity.getStoreId());
        if (store == null || (store.getDeleted() != null && store.getDeleted() == 1)) {
            throw new BusinessException("秒杀门店不存在");
        }
        if (store.getBusinessStatus() == null || store.getBusinessStatus() != 1) {
            throw new BusinessException("门店当前未营业");
        }
        if (!merchantService.isStoreOpenNow(store.getId(), LocalDateTime.now())) {
            throw new BusinessException("门店当前不在营业时段");
        }
        validateDeliveryScope(store, receiverAddress);

        ProductSku sku = productService.getSkuByIdFresh(activity.getSkuId());
        ProductSpu spu = productService.getSpuById(sku.getSpuId());
        if (!store.getId().equals(spu.getStoreId())) {
            throw new BusinessException("秒杀商品与门店不匹配");
        }
        if (spu.getSaleStatus() == null || spu.getSaleStatus() != 1 || sku.getStatus() == null || sku.getStatus() != 1) {
            throw new BusinessException("秒杀商品已下架");
        }

        int updated = productSkuMapper.deductStock(sku.getId(), 1, sku.getVersion());
        if (updated == 0) {
            throw new BusinessException("秒杀库存不足");
        }
        productService.evictSkuCache(sku.getId());

        BigDecimal deliveryFee = store.getDeliveryFee() == null ? BigDecimal.ZERO : store.getDeliveryFee();
        Order order = new Order();
        order.setOrderNo(redisIdGenerator.nextId("order"));
        order.setUserId(userId);
        order.setStoreId(store.getId());
        order.setMerchantId(store.getMerchantId());
        order.setOrderType(2);
        order.setOrderStatus(OrderStatus.PENDING_PAYMENT.name());
        order.setGoodsAmount(activity.getSeckillPrice());
        order.setDeliveryFee(deliveryFee);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayAmount(activity.getSeckillPrice().add(deliveryFee));
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setReceiverAddress(receiverAddress);
        order.setRemark(remark);
        orderMapper.insert(order);
        orderTraceService.initPendingPayment(order, "MOCK");

        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(order.getId());
        orderItem.setSkuId(sku.getId());
        orderItem.setSpuName(spu.getProductName());
        orderItem.setSkuName(sku.getSkuName());
        orderItem.setPrice(activity.getSeckillPrice());
        orderItem.setQuantity(1);
        orderItem.setTotalAmount(activity.getSeckillPrice());
        orderItemMapper.insert(orderItem);

        orderTraceService.log(order.getId(), order.getOrderStatus(), "SECKILL_CREATE", "STUDENT", userId, "秒杀异步创建订单成功");
        notifyStudent(order, "秒杀订单创建成功", "秒杀订单已创建，请尽快支付");
        return order;
    }

    private TradeContext buildTradeContext(Long userId, OrderPreviewRequest request, boolean freshSku) {
        Store store = storeMapper.selectById(request.getStoreId());
        if (store == null || (store.getDeleted() != null && store.getDeleted() == 1)) {
            throw new BusinessException("门店不存在");
        }
        if (store.getBusinessStatus() == null || store.getBusinessStatus() != 1) {
            throw new BusinessException("门店当前未营业");
        }
        if (!merchantService.isStoreOpenNow(store.getId(), LocalDateTime.now())) {
            throw new BusinessException("门店当前不在营业时段");
        }
        validateDeliveryScope(store, request.getReceiverAddress());

        List<TradeItem> tradeItems = new ArrayList<>();
        Map<Long, Integer> stockDeductPlan = new LinkedHashMap<>();
        Map<Long, Integer> skuStockSnapshot = new HashMap<>();
        BigDecimal goodsAmount = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            ProductSku sku = freshSku ? productService.getSkuByIdFresh(itemRequest.getSkuId()) : productService.getSkuByIdCached(itemRequest.getSkuId());
            ProductSpu spu = productService.getSpuById(sku.getSpuId());
            if (!store.getId().equals(spu.getStoreId())) {
                throw new BusinessException("订单中存在非当前门店商品");
            }
            if (spu.getSaleStatus() == null || spu.getSaleStatus() != 1 || sku.getStatus() == null || sku.getStatus() != 1) {
                throw new BusinessException("存在已下架商品");
            }
            mergeStockPlan(stockDeductPlan, skuStockSnapshot, sku, itemRequest.getQuantity(), "商品库存不足");

            if (spu.getProductType() != null && spu.getProductType() == 2) {
                List<ComboItem> comboItems = productService.listComboItemsBySpuId(spu.getId());
                if (comboItems.isEmpty()) {
                    throw new BusinessException("套餐明细不存在或已失效");
                }
                for (ComboItem comboItem : comboItems) {
                    ProductSku comboSku = freshSku
                            ? productService.getSkuByIdFresh(comboItem.getSkuId())
                            : productService.getSkuByIdCached(comboItem.getSkuId());
                    ProductSpu comboSpu = productService.getSpuById(comboSku.getSpuId());
                    if (!store.getId().equals(comboSpu.getStoreId())) {
                        throw new BusinessException("套餐中存在跨门店商品");
                    }
                    if (comboSpu.getSaleStatus() == null || comboSpu.getSaleStatus() != 1
                            || comboSku.getStatus() == null || comboSku.getStatus() != 1) {
                        throw new BusinessException("套餐中存在已下架商品");
                    }
                    mergeStockPlan(
                            stockDeductPlan,
                            skuStockSnapshot,
                            comboSku,
                            comboItem.getQuantity() * itemRequest.getQuantity(),
                            "套餐中存在库存不足商品"
                    );
                }
            }

            BigDecimal totalAmount = sku.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            goodsAmount = goodsAmount.add(totalAmount);
            tradeItems.add(new TradeItem(spu, sku, itemRequest.getQuantity(), totalAmount));
        }

        BigDecimal deliveryFee = store.getDeliveryFee() == null ? BigDecimal.ZERO : store.getDeliveryFee();
        if (store.getMinOrderAmount() != null && goodsAmount.compareTo(store.getMinOrderAmount()) < 0) {
            throw new BusinessException("未达到门店起送价");
        }
        BigDecimal amountBeforeDiscount = goodsAmount.add(deliveryFee);
        BigDecimal discountAmount = couponService.calculateDiscount(
                userId,
                request.getUserCouponId(),
                store.getId(),
                goodsAmount,
                amountBeforeDiscount
        );
        BigDecimal payAmount = amountBeforeDiscount.subtract(discountAmount);
        if (payAmount.signum() < 0) {
            payAmount = BigDecimal.ZERO;
        }
        List<StockDeductItem> stockDeductItems = stockDeductPlan.entrySet().stream()
                .map(entry -> new StockDeductItem(entry.getKey(), entry.getValue()))
                .toList();
        return new TradeContext(
                store,
                goodsAmount,
                deliveryFee,
                discountAmount,
                payAmount,
                tradeItems,
                stockDeductItems,
                request.getUserCouponId()
        );
    }

    private OrderPreviewResponse buildPreviewResponse(TradeContext tradeContext) {
        return OrderPreviewResponse.builder()
                .goodsAmount(tradeContext.goodsAmount())
                .deliveryFee(tradeContext.deliveryFee())
                .discountAmount(tradeContext.discountAmount())
                .payAmount(tradeContext.payAmount())
                .items(tradeContext.items().stream().map(item -> OrderPreviewSku.builder()
                        .skuId(item.sku().getId())
                        .productName(item.spu().getProductName())
                        .skuName(item.sku().getSkuName())
                        .price(item.sku().getPrice())
                        .quantity(item.quantity())
                        .totalAmount(item.totalAmount())
                        .build()).toList())
                .build();
    }

    private Order getOwnedOrder(Long userId, Long orderId) {
        Order order = getOrderOrThrow(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权限访问该订单");
        }
        return order;
    }

    private Order getMerchantOwnedOrder(Long merchantId, Long orderId) {
        Order order = getOrderOrThrow(orderId);
        if (!order.getMerchantId().equals(merchantId)) {
            throw new BusinessException("无权限操作该订单");
        }
        return order;
    }

    private Order getOrderOrThrow(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    private void restoreStock(Long orderId) {
        for (OrderItem orderItem : orderItemMapper.selectByOrderId(orderId)) {
            productSkuMapper.restoreStock(orderItem.getSkuId(), orderItem.getQuantity());
            productService.evictSkuCache(orderItem.getSkuId());

            ProductSku orderedSku = productSkuMapper.selectById(orderItem.getSkuId());
            if (orderedSku == null) {
                continue;
            }
            ProductSpu orderedSpu = productService.getSpuById(orderedSku.getSpuId());
            if (orderedSpu.getProductType() == null || orderedSpu.getProductType() != 2) {
                continue;
            }

            for (ComboItem comboItem : productService.listComboItemsBySpuId(orderedSpu.getId())) {
                int restoreQuantity = comboItem.getQuantity() * orderItem.getQuantity();
                productSkuMapper.restoreStock(comboItem.getSkuId(), restoreQuantity);
                productService.evictSkuCache(comboItem.getSkuId());
            }
        }
    }

    private void releaseSeckillReservation(Order order) {
        if (order.getOrderType() == null || order.getOrderType() != 2) {
            return;
        }
        SeckillOrder seckillOrder = seckillOrderMapper.selectByOrderId(order.getId());
        if (seckillOrder == null) {
            return;
        }
        seckillOrderMapper.deleteByOrderId(order.getId());
        stringRedisTemplate.opsForValue().increment("seckill:stock:" + seckillOrder.getActivityId());
        stringRedisTemplate.delete("seckill:ordered:" + seckillOrder.getActivityId() + ":" + order.getUserId());
    }

    private void mergeStockPlan(Map<Long, Integer> stockDeductPlan,
                                Map<Long, Integer> skuStockSnapshot,
                                ProductSku sku,
                                Integer addQuantity,
                                String shortageMsg) {
        int stock = sku.getStock() == null ? 0 : sku.getStock();
        skuStockSnapshot.putIfAbsent(sku.getId(), stock);
        int mergedQuantity = stockDeductPlan.getOrDefault(sku.getId(), 0) + addQuantity;
        if (mergedQuantity > skuStockSnapshot.get(sku.getId())) {
            throw new BusinessException(shortageMsg);
        }
        stockDeductPlan.put(sku.getId(), mergedQuantity);
    }

    private void validateDeliveryScope(Store store, String receiverAddress) {
        if (!StringUtils.hasText(store.getDeliveryScopeDesc())) {
            return;
        }
        if (!StringUtils.hasText(receiverAddress)) {
            throw new BusinessException("收货地址不能为空");
        }
        String[] scopes = store.getDeliveryScopeDesc().split("[,，;；/、\\s]+");
        for (String scope : scopes) {
            if (StringUtils.hasText(scope) && receiverAddress.contains(scope.trim())) {
                return;
            }
        }
        throw new BusinessException("收货地址不在门店配送范围内");
    }

    private boolean markAlertOnce(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(
                key,
                "1",
                Duration.ofMinutes(appOrderProperties.getAlertDedupMinutes())
        ));
    }

    private OrderDetailResponse toDetail(Order order) {
        Store store = storeMapper.selectById(order.getStoreId());
        List<OrderItemVO> items = orderItemMapper.selectByOrderId(order.getId()).stream()
                .map(item -> OrderItemVO.builder()
                        .skuId(item.getSkuId())
                        .spuName(item.getSpuName())
                        .skuName(item.getSkuName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .totalAmount(item.getTotalAmount())
                        .build())
                .toList();
        return OrderDetailResponse.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .orderStatus(order.getOrderStatus())
                .storeId(order.getStoreId())
                .storeName(store == null ? null : store.getStoreName())
                .payAmount(order.getPayAmount())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverAddress(order.getReceiverAddress())
                .remark(order.getRemark())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }

    private void notifyMerchant(String event, Order order, String message) {
        if (order == null) {
            return;
        }
        orderNotifier.notifyMerchant(OrderWsMessage.builder()
                .event(event)
                .merchantId(order.getMerchantId())
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .orderStatus(order.getOrderStatus())
                .message(message)
                .build());
    }

    private void notifyStudent(Order order, String title, String content) {
        if (order == null) {
            return;
        }
        studentMessageService.push(order.getUserId(), title, content, "ORDER", "ORDER", order.getId());
    }

    private void ensureDeliveryPoolOrder(Long orderId) {
        DeliveryOrder existed = deliveryOrderMapper.selectByOrderId(orderId);
        if (existed != null) {
            return;
        }
        DeliveryOrder deliveryOrder = new DeliveryOrder();
        deliveryOrder.setOrderId(orderId);
        deliveryOrder.setDeliveryUserId(UNASSIGNED_DELIVERY_USER_ID);
        deliveryOrder.setDispatchStatus(0);
        deliveryOrderMapper.insert(deliveryOrder);
    }

    private DeliveryOrderResponse toDeliveryResponse(DeliveryOrder deliveryOrder) {
        Order order = getOrderOrThrow(deliveryOrder.getOrderId());
        Store store = storeMapper.selectById(order.getStoreId());
        return DeliveryOrderResponse.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .storeId(order.getStoreId())
                .storeName(store == null ? null : store.getStoreName())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverAddress(order.getReceiverAddress())
                .orderStatus(order.getOrderStatus())
                .dispatchStatus(deliveryOrder.getDispatchStatus())
                .dispatchRemark(deliveryOrder.getDispatchRemark())
                .payAmount(order.getPayAmount())
                .createdAt(order.getCreatedAt())
                .pickupTime(deliveryOrder.getPickupTime())
                .deliveredTime(deliveryOrder.getDeliveredTime())
                .build();
    }

    private record TradeContext(Store store,
                                BigDecimal goodsAmount,
                                BigDecimal deliveryFee,
                                BigDecimal discountAmount,
                                BigDecimal payAmount,
                                List<TradeItem> items,
                                List<StockDeductItem> stockDeductItems,
                                Long userCouponId) {
    }

    private record TradeItem(ProductSpu spu,
                             ProductSku sku,
                             Integer quantity,
                             BigDecimal totalAmount) {
    }

    private record StockDeductItem(Long skuId, Integer quantity) {
    }
}
