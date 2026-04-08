package com.zhinengpt.campuscatering.order.controller;

import com.zhinengpt.campuscatering.common.response.ApiResponse;
import com.zhinengpt.campuscatering.order.dto.OrderCreateResponse;
import com.zhinengpt.campuscatering.order.dto.OrderDetailResponse;
import com.zhinengpt.campuscatering.order.dto.OrderPreviewRequest;
import com.zhinengpt.campuscatering.order.dto.OrderPreviewResponse;
import com.zhinengpt.campuscatering.order.dto.OrderReviewRequest;
import com.zhinengpt.campuscatering.order.dto.OrderReviewResponse;
import com.zhinengpt.campuscatering.order.service.OrderReviewService;
import com.zhinengpt.campuscatering.order.service.OrderService;
import com.zhinengpt.campuscatering.security.SecurityUtils;
import com.zhinengpt.campuscatering.trace.dto.OrderLogResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/order")
@PreAuthorize("hasRole('STUDENT')")
public class StudentOrderController {

    private final OrderService orderService;
    private final OrderReviewService orderReviewService;

    public StudentOrderController(OrderService orderService, OrderReviewService orderReviewService) {
        this.orderService = orderService;
        this.orderReviewService = orderReviewService;
    }

    @PostMapping("/preview")
    public ApiResponse<OrderPreviewResponse> preview(@Valid @RequestBody OrderPreviewRequest request) {
        return ApiResponse.success(orderService.preview(SecurityUtils.getLoginUser().getUserId(), request));
    }

    @PostMapping("/create")
    public ApiResponse<OrderCreateResponse> create(@Valid @RequestBody OrderPreviewRequest request) {
        return ApiResponse.success(orderService.create(SecurityUtils.getLoginUser().getUserId(), request));
    }

    @PostMapping("/pay/{orderId}")
    public ApiResponse<Void> pay(@PathVariable Long orderId) {
        orderService.pay(SecurityUtils.getLoginUser().getUserId(), orderId);
        return ApiResponse.success();
    }

    @PostMapping("/cancel/{orderId}")
    public ApiResponse<Void> cancel(@PathVariable Long orderId) {
        orderService.cancelByStudent(SecurityUtils.getLoginUser().getUserId(), orderId);
        return ApiResponse.success();
    }

    @PostMapping("/refund/{orderId}")
    public ApiResponse<Void> refund(@PathVariable Long orderId) {
        orderService.requestRefund(SecurityUtils.getLoginUser().getUserId(), orderId);
        return ApiResponse.success();
    }

    @PostMapping("/reorder/{orderId}")
    public ApiResponse<OrderCreateResponse> reorder(@PathVariable Long orderId) {
        return ApiResponse.success(orderService.reorder(SecurityUtils.getLoginUser().getUserId(), orderId));
    }

    @PostMapping("/urge/{orderId}")
    public ApiResponse<Void> urge(@PathVariable Long orderId) {
        orderService.urge(SecurityUtils.getLoginUser().getUserId(), orderId);
        return ApiResponse.success();
    }

    @PostMapping("/review")
    public ApiResponse<Void> review(@Valid @RequestBody OrderReviewRequest request) {
        orderReviewService.review(SecurityUtils.getLoginUser().getUserId(), request);
        return ApiResponse.success();
    }

    @GetMapping("/review/{orderId}")
    public ApiResponse<OrderReviewResponse> reviewDetail(@PathVariable Long orderId) {
        return ApiResponse.success(orderReviewService.detail(SecurityUtils.getLoginUser().getUserId(), orderId));
    }

    @GetMapping("/list")
    public ApiResponse<List<OrderDetailResponse>> list() {
        return ApiResponse.success(orderService.listStudentOrders(SecurityUtils.getLoginUser().getUserId()));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailResponse> detail(@PathVariable Long orderId) {
        return ApiResponse.success(orderService.studentOrderDetail(SecurityUtils.getLoginUser().getUserId(), orderId));
    }

    @GetMapping("/logs/{orderId}")
    public ApiResponse<List<OrderLogResponse>> logs(@PathVariable Long orderId) {
        return ApiResponse.success(orderService.studentOrderLogs(SecurityUtils.getLoginUser().getUserId(), orderId));
    }
}
