package com.zhinengpt.campuscatering.merchant.controller;

import com.zhinengpt.campuscatering.common.response.ApiResponse;
import com.zhinengpt.campuscatering.merchant.dto.MerchantApplyRequest;
import com.zhinengpt.campuscatering.merchant.dto.StoreBusinessHourResponse;
import com.zhinengpt.campuscatering.merchant.dto.StoreBusinessHoursSaveRequest;
import com.zhinengpt.campuscatering.merchant.dto.StoreSimpleResponse;
import com.zhinengpt.campuscatering.merchant.dto.StoreUpdateRequest;
import com.zhinengpt.campuscatering.merchant.service.MerchantService;
import com.zhinengpt.campuscatering.order.service.OrderService;
import com.zhinengpt.campuscatering.security.SecurityUtils;
import com.zhinengpt.campuscatering.statistics.dto.MerchantHotProductResponse;
import com.zhinengpt.campuscatering.statistics.dto.MerchantOverviewResponse;
import com.zhinengpt.campuscatering.statistics.dto.MerchantStatusDistributionResponse;
import com.zhinengpt.campuscatering.statistics.dto.MerchantTrendPointResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/merchant")
public class MerchantController {

    private final MerchantService merchantService;
    private final OrderService orderService;

    public MerchantController(MerchantService merchantService, OrderService orderService) {
        this.merchantService = merchantService;
        this.orderService = orderService;
    }

    @PostMapping("/apply")
    public ApiResponse<Long> apply(@Valid @RequestBody MerchantApplyRequest request) {
        return ApiResponse.success(merchantService.submitApply(request));
    }

    @GetMapping("/store/detail")
    @PreAuthorize("hasRole('MERCHANT')")
    public ApiResponse<StoreSimpleResponse> storeDetail() {
        return ApiResponse.success(merchantService.getMerchantStore(SecurityUtils.getLoginUser().getMerchantId()));
    }

    @PutMapping("/store/update")
    @PreAuthorize("hasRole('MERCHANT')")
    public ApiResponse<Void> updateStore(@Valid @RequestBody StoreUpdateRequest request) {
        merchantService.updateMerchantStore(SecurityUtils.getLoginUser().getMerchantId(), request);
        return ApiResponse.success();
    }

    @GetMapping("/store/business-hours")
    @PreAuthorize("hasRole('MERCHANT')")
    public ApiResponse<List<StoreBusinessHourResponse>> businessHours() {
        return ApiResponse.success(merchantService.listBusinessHours(SecurityUtils.getLoginUser().getMerchantId()));
    }

    @PutMapping("/store/business-hours")
    @PreAuthorize("hasRole('MERCHANT')")
    public ApiResponse<Void> saveBusinessHours(@Valid @RequestBody StoreBusinessHoursSaveRequest request) {
        merchantService.saveBusinessHours(SecurityUtils.getLoginUser().getMerchantId(), request);
        return ApiResponse.success();
    }

    @GetMapping("/statistics/overview")
    @PreAuthorize("hasRole('MERCHANT')")
    public ApiResponse<MerchantOverviewResponse> overview() {
        return ApiResponse.success(orderService.merchantOverview(SecurityUtils.getLoginUser().getMerchantId()));
    }

    @GetMapping("/statistics/trend")
    @PreAuthorize("hasRole('MERCHANT')")
    public ApiResponse<List<MerchantTrendPointResponse>> trend(@RequestParam(required = false) Integer days) {
        return ApiResponse.success(orderService.merchantTrend(SecurityUtils.getLoginUser().getMerchantId(), days == null ? 7 : days));
    }

    @GetMapping("/statistics/status-distribution")
    @PreAuthorize("hasRole('MERCHANT')")
    public ApiResponse<List<MerchantStatusDistributionResponse>> statusDistribution(@RequestParam(required = false) Integer days) {
        return ApiResponse.success(orderService.merchantStatusDistribution(SecurityUtils.getLoginUser().getMerchantId(), days == null ? 7 : days));
    }

    @GetMapping("/statistics/hot-products")
    @PreAuthorize("hasRole('MERCHANT')")
    public ApiResponse<List<MerchantHotProductResponse>> hotProducts(@RequestParam(required = false) Integer days,
                                                                     @RequestParam(required = false) Integer limit) {
        return ApiResponse.success(orderService.merchantHotProducts(
                SecurityUtils.getLoginUser().getMerchantId(),
                days == null ? 7 : days,
                limit == null ? 10 : limit
        ));
    }
}
