package com.zhinengpt.campuscatering.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhinengpt.campuscatering.common.enums.OrderStatus;
import com.zhinengpt.campuscatering.order.entity.Order;
import com.zhinengpt.campuscatering.order.mapper.OrderMapper;
import com.zhinengpt.campuscatering.trace.entity.PaymentRecord;
import com.zhinengpt.campuscatering.trace.mapper.PaymentRecordMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PaymentRecordMapper paymentRecordMapper;

    @Test
    void requestRefund_marksPaidOrderAsRefundedAndUpdatesPaymentRecord() {
        Order order = new Order();
        order.setOrderNo(202604080188L);
        order.setUserId(1L);
        order.setStoreId(1L);
        order.setMerchantId(1L);
        order.setOrderType(1);
        order.setOrderStatus(OrderStatus.PAID.name());
        order.setGoodsAmount(new BigDecimal("18.00"));
        order.setDeliveryFee(new BigDecimal("2.00"));
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayAmount(new BigDecimal("20.00"));
        order.setReceiverName("张三");
        order.setReceiverPhone("13800000001");
        order.setReceiverAddress("东区宿舍 3-402");
        order.setPayTime(LocalDateTime.now());
        orderMapper.insert(order);

        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setOrderId(order.getId());
        paymentRecord.setOrderNo(order.getOrderNo());
        paymentRecord.setPayAmount(order.getPayAmount());
        paymentRecord.setPayChannel("MOCK");
        paymentRecord.setPayStatus("SUCCESS");
        paymentRecord.setTransactionNo("MOCK-" + order.getOrderNo());
        paymentRecord.setPaidAt(LocalDateTime.now());
        paymentRecordMapper.insert(paymentRecord);

        orderService.requestRefund(1L, order.getId());

        Order latest = orderMapper.selectById(order.getId());
        PaymentRecord refundedRecord = paymentRecordMapper.selectByOrderId(order.getId());

        assertEquals(OrderStatus.REFUNDED.name(), latest.getOrderStatus());
        assertNotNull(refundedRecord);
        assertEquals("REFUNDED", refundedRecord.getPayStatus());
        assertNotNull(refundedRecord.getTransactionNo());
        assertTrue(refundedRecord.getTransactionNo().startsWith("REFUND-"));
    }
}
