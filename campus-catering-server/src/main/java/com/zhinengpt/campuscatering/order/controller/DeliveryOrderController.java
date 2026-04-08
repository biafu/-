package com.zhinengpt.campuscatering.order.controller;

import com.zhinengpt.campuscatering.common.response.ApiResponse;
import com.zhinengpt.campuscatering.order.dto.DeliveryOrderResponse;
import com.zhinengpt.campuscatering.order.service.OrderService;
import com.zhinengpt.campuscatering.security.SecurityUtils;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/delivery/order")
@PreAuthorize("hasRole('DELIVERY')")
public class DeliveryOrderController {

    private final OrderService orderService;

    public DeliveryOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/list")
    public ApiResponse<List<DeliveryOrderResponse>> list() {
        return ApiResponse.success(orderService.listDeliveryOrders(SecurityUtils.getLoginUser().getUserId()));
    }

    @GetMapping("/available")
    public ApiResponse<List<DeliveryOrderResponse>> available() {
        return ApiResponse.success(orderService.listAvailableDeliveryOrders());
    }

    @PostMapping("/claim/{orderId}")
    public ApiResponse<Void> claim(@PathVariable Long orderId) {
        orderService.claimDeliveryOrder(SecurityUtils.getLoginUser().getUserId(), orderId);
        return ApiResponse.success();
    }

    @PostMapping("/pickup/{orderId}")
    public ApiResponse<Void> pickup(@PathVariable Long orderId) {
        orderService.pickupDeliveryOrder(SecurityUtils.getLoginUser().getUserId(), orderId);
        return ApiResponse.success();
    }

    @PostMapping("/complete/{orderId}")
    public ApiResponse<Void> complete(@PathVariable Long orderId) {
        orderService.completeDeliveryByUser(SecurityUtils.getLoginUser().getUserId(), orderId);
        return ApiResponse.success();
    }
}
