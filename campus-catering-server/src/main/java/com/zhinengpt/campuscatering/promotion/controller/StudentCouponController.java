package com.zhinengpt.campuscatering.promotion.controller;

import com.zhinengpt.campuscatering.common.response.ApiResponse;
import com.zhinengpt.campuscatering.promotion.dto.CouponCenterResponse;
import com.zhinengpt.campuscatering.promotion.dto.MyCouponResponse;
import com.zhinengpt.campuscatering.promotion.service.CouponService;
import com.zhinengpt.campuscatering.security.SecurityUtils;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/coupon")
@PreAuthorize("hasRole('STUDENT')")
public class StudentCouponController {

    private final CouponService couponService;

    public StudentCouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping("/center")
    public ApiResponse<List<CouponCenterResponse>> center(@RequestParam(required = false) Long storeId) {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        return ApiResponse.success(couponService.claimableCoupons(userId, storeId));
    }

    @PostMapping("/claim/{couponId}")
    public ApiResponse<Void> claim(@PathVariable Long couponId) {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        couponService.claim(userId, couponId);
        return ApiResponse.success();
    }

    @GetMapping("/my")
    public ApiResponse<List<MyCouponResponse>> myCoupons(@RequestParam(required = false) Long storeId) {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        return ApiResponse.success(couponService.usableCoupons(userId, storeId));
    }
}
