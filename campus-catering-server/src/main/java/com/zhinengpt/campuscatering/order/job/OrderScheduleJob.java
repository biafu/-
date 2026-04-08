package com.zhinengpt.campuscatering.order.job;

import com.zhinengpt.campuscatering.order.service.OrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderScheduleJob {

    private final OrderService orderService;

    public OrderScheduleJob(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(fixedDelay = 60000)
    public void cancelTimeoutOrders() {
        orderService.cancelTimeoutOrders();
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 10000)
    public void merchantTimeoutAlertJob() {
        orderService.merchantTimeoutAlert();
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 20000)
    public void deliveryTimeoutJob() {
        orderService.deliveryTimeoutAlert();
    }
}
