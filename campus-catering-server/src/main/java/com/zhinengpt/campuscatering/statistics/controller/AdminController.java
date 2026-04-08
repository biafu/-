package com.zhinengpt.campuscatering.statistics.controller;

import com.zhinengpt.campuscatering.common.response.ApiResponse;
import com.zhinengpt.campuscatering.merchant.dto.MerchantAuditRequest;
import com.zhinengpt.campuscatering.merchant.entity.MerchantApply;
import com.zhinengpt.campuscatering.order.dto.DispatchRequest;
import com.zhinengpt.campuscatering.seckill.dto.SeckillActivityResponse;
import com.zhinengpt.campuscatering.seckill.dto.SeckillActivitySaveRequest;
import com.zhinengpt.campuscatering.statistics.dto.DailyStatisticsResponse;
import com.zhinengpt.campuscatering.statistics.dto.DashboardOverviewResponse;
import com.zhinengpt.campuscatering.statistics.dto.MerchantRankResponse;
import com.zhinengpt.campuscatering.statistics.service.AdminService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/merchant/apply/list")
    public ApiResponse<List<MerchantApply>> listApplications() {
        return ApiResponse.success(adminService.listApplications());
    }

    @PostMapping("/merchant/apply/audit")
    public ApiResponse<Void> audit(@Valid @RequestBody MerchantAuditRequest request) {
        adminService.auditMerchant(request);
        return ApiResponse.success();
    }

    @PostMapping("/delivery/dispatch")
    public ApiResponse<Void> dispatch(@Valid @RequestBody DispatchRequest request) {
        adminService.dispatch(request);
        return ApiResponse.success();
    }

    @PostMapping("/delivery/complete/{orderId}")
    public ApiResponse<Void> completeDelivery(@PathVariable Long orderId) {
        adminService.completeDelivery(orderId);
        return ApiResponse.success();
    }

    @PostMapping("/seckill/activity/save")
    public ApiResponse<Long> saveSeckillActivity(@Valid @RequestBody SeckillActivitySaveRequest request) {
        return ApiResponse.success(adminService.saveSeckillActivity(request));
    }

    @GetMapping("/seckill/activity/list")
    public ApiResponse<List<SeckillActivityResponse>> listSeckillActivities() {
        return ApiResponse.success(adminService.listSeckillActivities());
    }

    @DeleteMapping("/seckill/activity/{activityId}")
    public ApiResponse<Void> deleteSeckillActivity(@PathVariable Long activityId) {
        adminService.deleteSeckillActivity(activityId);
        return ApiResponse.success();
    }

    @GetMapping("/statistics/dashboard")
    public ApiResponse<DashboardOverviewResponse> dashboard() {
        return ApiResponse.success(adminService.dashboard());
    }

    @GetMapping("/statistics/daily")
    public ApiResponse<List<DailyStatisticsResponse>> dailyStatistics(@RequestParam(defaultValue = "7") Integer days) {
        return ApiResponse.success(adminService.listDailyStatistics(days));
    }

    @PostMapping("/statistics/daily/rebuild")
    public ApiResponse<Void> rebuildDailyStatistics(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate statDate) {
        adminService.rebuildDailyStatistics(statDate);
        return ApiResponse.success();
    }

    @GetMapping("/statistics/merchant-rank")
    public ApiResponse<List<MerchantRankResponse>> merchantRank(@RequestParam(defaultValue = "7") Integer days,
                                                                @RequestParam(defaultValue = "10") Integer limit) {
        return ApiResponse.success(adminService.merchantRank(days, limit));
    }

    @PostMapping("/store/enable/{storeId}")
    public ApiResponse<Void> enableStore(@PathVariable Long storeId) {
        adminService.enableStore(storeId);
        return ApiResponse.success();
    }

    @PostMapping("/store/disable/{storeId}")
    public ApiResponse<Void> disableStore(@PathVariable Long storeId) {
        adminService.disableStore(storeId);
        return ApiResponse.success();
    }
}
