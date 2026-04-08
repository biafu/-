package com.zhinengpt.campuscatering.merchant.service;

import com.zhinengpt.campuscatering.auth.entity.MerchantUser;
import com.zhinengpt.campuscatering.auth.mapper.MerchantUserMapper;
import com.zhinengpt.campuscatering.cache.CacheClient;
import com.zhinengpt.campuscatering.common.exception.BusinessException;
import com.zhinengpt.campuscatering.merchant.dto.MerchantApplyRequest;
import com.zhinengpt.campuscatering.merchant.dto.MerchantAuditRequest;
import com.zhinengpt.campuscatering.merchant.dto.StoreBusinessHourItemRequest;
import com.zhinengpt.campuscatering.merchant.dto.StoreBusinessHourResponse;
import com.zhinengpt.campuscatering.merchant.dto.StoreBusinessHoursSaveRequest;
import com.zhinengpt.campuscatering.merchant.dto.StoreSimpleResponse;
import com.zhinengpt.campuscatering.merchant.dto.StoreUpdateRequest;
import com.zhinengpt.campuscatering.merchant.entity.Merchant;
import com.zhinengpt.campuscatering.merchant.entity.MerchantApply;
import com.zhinengpt.campuscatering.merchant.entity.Store;
import com.zhinengpt.campuscatering.merchant.entity.StoreBusinessHours;
import com.zhinengpt.campuscatering.merchant.mapper.MerchantApplyMapper;
import com.zhinengpt.campuscatering.merchant.mapper.MerchantMapper;
import com.zhinengpt.campuscatering.merchant.mapper.StoreBusinessHoursMapper;
import com.zhinengpt.campuscatering.merchant.mapper.StoreMapper;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MerchantService {

    private static final String STORE_CACHE_PREFIX = "store:detail:";

    private final MerchantApplyMapper merchantApplyMapper;
    private final MerchantMapper merchantMapper;
    private final MerchantUserMapper merchantUserMapper;
    private final StoreMapper storeMapper;
    private final StoreBusinessHoursMapper storeBusinessHoursMapper;
    private final CacheClient cacheClient;

    public MerchantService(MerchantApplyMapper merchantApplyMapper,
                           MerchantMapper merchantMapper,
                           MerchantUserMapper merchantUserMapper,
                           StoreMapper storeMapper,
                           StoreBusinessHoursMapper storeBusinessHoursMapper,
                           CacheClient cacheClient) {
        this.merchantApplyMapper = merchantApplyMapper;
        this.merchantMapper = merchantMapper;
        this.merchantUserMapper = merchantUserMapper;
        this.storeMapper = storeMapper;
        this.storeBusinessHoursMapper = storeBusinessHoursMapper;
        this.cacheClient = cacheClient;
    }

    public Long submitApply(MerchantApplyRequest request) {
        MerchantApply apply = new MerchantApply();
        apply.setMerchantName(request.getMerchantName());
        apply.setContactName(request.getContactName());
        apply.setContactPhone(request.getContactPhone());
        apply.setCampusCode(request.getCampusCode());
        apply.setLicenseNo(request.getLicenseNo());
        apply.setStatus(0);
        merchantApplyMapper.insert(apply);
        return apply.getId();
    }

    public List<MerchantApply> listApplications() {
        return merchantApplyMapper.selectAll();
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditApply(MerchantAuditRequest request) {
        MerchantApply apply = merchantApplyMapper.selectById(request.getApplyId());
        if (apply == null) {
            throw new BusinessException("入驻申请不存在");
        }
        if (apply.getStatus() != null && apply.getStatus() != 0) {
            throw new BusinessException("申请已审核，请勿重复操作");
        }

        apply.setStatus(Boolean.TRUE.equals(request.getApproved()) ? 1 : 2);
        apply.setAuditRemark(request.getAuditRemark());
        merchantApplyMapper.updateAudit(apply);
        if (!Boolean.TRUE.equals(request.getApproved())) {
            return;
        }

        Merchant merchant = new Merchant();
        merchant.setMerchantName(apply.getMerchantName());
        merchant.setContactName(apply.getContactName());
        merchant.setContactPhone(apply.getContactPhone());
        merchant.setCampusCode(apply.getCampusCode());
        merchant.setStatus(1);
        merchant.setSettleType(1);
        merchantMapper.insert(merchant);

        String campusName = resolveCampusName(apply.getCampusCode());
        Store store = new Store();
        store.setMerchantId(merchant.getId());
        store.setStoreName(apply.getMerchantName());
        store.setAddress(campusName + "商家中心待完善详细地址");
        store.setDeliveryType(2);
        store.setDeliveryScopeDesc(campusName + "教学区、宿舍区");
        store.setDeliveryRadiusKm(new BigDecimal("3.00"));
        store.setMinOrderAmount(new BigDecimal("15.00"));
        store.setDeliveryFee(new BigDecimal("2.00"));
        store.setBusinessStatus(0);
        store.setNotice("新店已入驻，请先核对门店信息后再开始营业");
        storeMapper.insert(store);
        saveDefaultBusinessHours(store.getId());

        MerchantUser merchantUser = new MerchantUser();
        merchantUser.setMerchantId(merchant.getId());
        merchantUser.setUsername(apply.getContactPhone());
        merchantUser.setPassword("{noop}123456");
        merchantUser.setRealName(apply.getContactName());
        merchantUser.setStatus(1);
        merchantUserMapper.insert(merchantUser);
    }

    public List<StoreSimpleResponse> listStores() {
        return storeMapper.selectAll().stream()
                .map(this::toStoreSimpleResponse)
                .toList();
    }

    public StoreSimpleResponse getStoreDetail(Long storeId) {
        Store store = cacheClient.queryWithPassThrough(
                STORE_CACHE_PREFIX + storeId,
                Store.class,
                Duration.ofMinutes(60),
                Duration.ofMinutes(3),
                ignored -> storeMapper.selectById(storeId)
        );
        if (store == null) {
            throw new BusinessException("门店不存在");
        }
        return toStoreSimpleResponse(store);
    }

    public StoreSimpleResponse getMerchantStore(Long merchantId) {
        return toStoreSimpleResponse(findStoreByMerchantId(merchantId));
    }

    public void updateMerchantStore(Long merchantId, StoreUpdateRequest request) {
        Store store = findStoreByMerchantId(merchantId);
        store.setStoreName(request.getStoreName());
        store.setAddress(request.getAddress());
        store.setDeliveryType(request.getDeliveryType());
        store.setDeliveryScopeDesc(request.getDeliveryScopeDesc());
        store.setDeliveryRadiusKm(request.getDeliveryRadiusKm());
        store.setMinOrderAmount(request.getMinOrderAmount());
        store.setDeliveryFee(request.getDeliveryFee());
        store.setBusinessStatus(request.getBusinessStatus());
        store.setNotice(request.getNotice());
        storeMapper.updateByMerchantId(store);
        evictStoreCache(store.getId());
    }

    public void updateStoreBusinessStatus(Long storeId, Integer status) {
        storeMapper.updateBusinessStatus(storeId, status);
        evictStoreCache(storeId);
    }

    public Store findStoreByMerchantId(Long merchantId) {
        Store store = storeMapper.selectByMerchantId(merchantId);
        if (store == null) {
            throw new BusinessException("商户门店不存在，请先完善入驻信息");
        }
        return store;
    }

    public Store findStoreById(Long storeId) {
        Store store = storeMapper.selectById(storeId);
        if (store == null) {
            throw new BusinessException("门店不存在");
        }
        return store;
    }

    public List<StoreBusinessHourResponse> listBusinessHours(Long merchantId) {
        Store store = findStoreByMerchantId(merchantId);
        List<StoreBusinessHours> rows = storeBusinessHoursMapper.selectByStoreId(store.getId());
        return rows.stream()
                .map(item -> StoreBusinessHourResponse.builder()
                        .id(item.getId())
                        .dayOfWeek(item.getDayOfWeek())
                        .startTime(item.getStartTime())
                        .endTime(item.getEndTime())
                        .status(item.getStatus())
                        .build())
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveBusinessHours(Long merchantId, StoreBusinessHoursSaveRequest request) {
        Store store = findStoreByMerchantId(merchantId);
        validateBusinessHours(request.getHours());
        storeBusinessHoursMapper.deleteByStoreId(store.getId());
        for (StoreBusinessHourItemRequest hour : request.getHours()) {
            StoreBusinessHours row = new StoreBusinessHours();
            row.setStoreId(store.getId());
            row.setDayOfWeek(hour.getDayOfWeek());
            row.setStartTime(hour.getStartTime());
            row.setEndTime(hour.getEndTime());
            row.setStatus(hour.getStatus());
            storeBusinessHoursMapper.insert(row);
        }
    }

    public boolean isStoreOpenNow(Long storeId, LocalDateTime now) {
        List<StoreBusinessHours> hours = storeBusinessHoursMapper.selectActiveByStoreAndDay(
                storeId,
                now.getDayOfWeek().getValue()
        );
        if (hours.isEmpty()) {
            return true;
        }
        LocalTime currentTime = now.toLocalTime();
        for (StoreBusinessHours slot : hours) {
            if (!currentTime.isBefore(slot.getStartTime()) && currentTime.isBefore(slot.getEndTime())) {
                return true;
            }
        }
        return false;
    }

    private void evictStoreCache(Long storeId) {
        cacheClient.delete(STORE_CACHE_PREFIX + storeId);
    }

    private String resolveCampusName(String campusCode) {
        if (campusCode == null) {
            return "校园";
        }
        return switch (campusCode.trim().toUpperCase()) {
            case "MAIN" -> "主校区";
            case "NORTH" -> "北校区";
            case "SOUTH" -> "南校区";
            case "EAST" -> "东校区";
            case "WEST" -> "西校区";
            default -> campusCode.trim();
        };
    }

    private void saveDefaultBusinessHours(Long storeId) {
        for (int dayOfWeek = 1; dayOfWeek <= 7; dayOfWeek++) {
            StoreBusinessHours row = new StoreBusinessHours();
            row.setStoreId(storeId);
            row.setDayOfWeek(dayOfWeek);
            row.setStartTime(LocalTime.of(7, 0));
            row.setEndTime(LocalTime.of(21, 0));
            row.setStatus(1);
            storeBusinessHoursMapper.insert(row);
        }
    }

    private void validateBusinessHours(List<StoreBusinessHourItemRequest> hours) {
        for (StoreBusinessHourItemRequest item : hours) {
            if (!item.getStartTime().isBefore(item.getEndTime())) {
                throw new BusinessException("营业时间必须满足开始时间早于结束时间");
            }
        }
        Map<Integer, List<StoreBusinessHourItemRequest>> dayGroup = hours.stream()
                .filter(item -> item.getStatus() != null && item.getStatus() == 1)
                .collect(java.util.stream.Collectors.groupingBy(StoreBusinessHourItemRequest::getDayOfWeek));
        for (Map.Entry<Integer, List<StoreBusinessHourItemRequest>> entry : dayGroup.entrySet()) {
            List<StoreBusinessHourItemRequest> sorted = new ArrayList<>(entry.getValue());
            sorted.sort(Comparator.comparing(StoreBusinessHourItemRequest::getStartTime));
            for (int i = 1; i < sorted.size(); i++) {
                if (sorted.get(i - 1).getEndTime().isAfter(sorted.get(i).getStartTime())) {
                    throw new BusinessException("同一天的营业时间段存在重叠");
                }
            }
        }
    }

    private StoreSimpleResponse toStoreSimpleResponse(Store store) {
        return StoreSimpleResponse.builder()
                .id(store.getId())
                .merchantId(store.getMerchantId())
                .storeName(store.getStoreName())
                .address(store.getAddress())
                .deliveryScopeDesc(store.getDeliveryScopeDesc())
                .deliveryRadiusKm(store.getDeliveryRadiusKm())
                .minOrderAmount(store.getMinOrderAmount())
                .deliveryFee(store.getDeliveryFee())
                .businessStatus(store.getBusinessStatus())
                .notice(store.getNotice())
                .build();
    }
}
