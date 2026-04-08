package com.zhinengpt.campuscatering.order.controller;

import com.zhinengpt.campuscatering.common.response.ApiResponse;
import com.zhinengpt.campuscatering.order.dto.MerchantReviewResponse;
import com.zhinengpt.campuscatering.order.service.OrderReviewService;
import com.zhinengpt.campuscatering.security.SecurityUtils;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/merchant/review")
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantReviewController {

    private final OrderReviewService orderReviewService;

    public MerchantReviewController(OrderReviewService orderReviewService) {
        this.orderReviewService = orderReviewService;
    }

    @GetMapping("/list")
    public ApiResponse<List<MerchantReviewResponse>> list() {
        return ApiResponse.success(orderReviewService.listMerchantReviews(SecurityUtils.getLoginUser().getMerchantId()));
    }
}
