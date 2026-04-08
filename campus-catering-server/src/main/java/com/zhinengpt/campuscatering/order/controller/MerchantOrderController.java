package com.zhinengpt.campuscatering.order.controller;

import com.zhinengpt.campuscatering.common.response.ApiResponse;
import com.zhinengpt.campuscatering.order.dto.OrderDetailResponse;
import com.zhinengpt.campuscatering.order.dto.OrderExceptionHandleRequest;
import com.zhinengpt.campuscatering.order.dto.OrderExceptionResolveRequest;
import com.zhinengpt.campuscatering.order.dto.OrderExceptionResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/merchant/order")
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantOrderController {

    private final OrderService orderService;

    public MerchantOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/list")
    public ApiResponse<List<OrderDetailResponse>> list() {
        return ApiResponse.success(orderService.listMerchantOrders(SecurityUtils.getLoginUser().getMerchantId()));
    }

    @PostMapping("/accept/{orderId}")
    public ApiResponse<Void> accept(@PathVariable Long orderId) {
        orderService.accept(SecurityUtils.getLoginUser().getMerchantId(), orderId);
        return ApiResponse.success();
    }

    @PostMapping("/reject/{orderId}")
    public ApiResponse<Void> reject(@PathVariable Long orderId) {
        orderService.reject(SecurityUtils.getLoginUser().getMerchantId(), orderId);
        return ApiResponse.success();
    }

    @PostMapping("/prepare/{orderId}")
    public ApiResponse<Void> prepare(@PathVariable Long orderId) {
        orderService.prepare(SecurityUtils.getLoginUser().getMerchantId(), orderId);
        return ApiResponse.success();
    }

    @PostMapping("/finish/{orderId}")
    public ApiResponse<Void> finish(@PathVariable Long orderId) {
        orderService.finishPrepare(SecurityUtils.getLoginUser().getMerchantId(), orderId);
        return ApiResponse.success();
    }

    @GetMapping("/logs/{orderId}")
    public ApiResponse<List<OrderLogResponse>> logs(@PathVariable Long orderId) {
        return ApiResponse.success(orderService.merchantOrderLogs(SecurityUtils.getLoginUser().getMerchantId(), orderId));
    }

    @PostMapping("/exception/report")
    public ApiResponse<Void> reportException(@Valid @RequestBody OrderExceptionHandleRequest request) {
        orderService.reportException(SecurityUtils.getLoginUser().getMerchantId(), request);
        return ApiResponse.success();
    }

    @PostMapping("/exception/resolve")
    public ApiResponse<Void> resolveException(@Valid @RequestBody OrderExceptionResolveRequest request) {
        orderService.resolveException(SecurityUtils.getLoginUser().getMerchantId(), request);
        return ApiResponse.success();
    }

    @GetMapping("/exception/list")
    public ApiResponse<List<OrderExceptionResponse>> exceptionList(@RequestParam(required = false) String status) {
        return ApiResponse.success(orderService.listExceptions(SecurityUtils.getLoginUser().getMerchantId(), status));
    }
}
