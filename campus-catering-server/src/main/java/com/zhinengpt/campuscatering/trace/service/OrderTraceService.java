package com.zhinengpt.campuscatering.trace.service;

import com.zhinengpt.campuscatering.order.entity.Order;
import com.zhinengpt.campuscatering.trace.dto.OrderLogResponse;
import com.zhinengpt.campuscatering.trace.entity.OrderLog;
import com.zhinengpt.campuscatering.trace.entity.PaymentRecord;
import com.zhinengpt.campuscatering.trace.mapper.OrderLogMapper;
import com.zhinengpt.campuscatering.trace.mapper.PaymentRecordMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderTraceService {

    private final OrderLogMapper orderLogMapper;
    private final PaymentRecordMapper paymentRecordMapper;

    public OrderTraceService(OrderLogMapper orderLogMapper, PaymentRecordMapper paymentRecordMapper) {
        this.orderLogMapper = orderLogMapper;
        this.paymentRecordMapper = paymentRecordMapper;
    }

    public void log(Long orderId, String orderStatus, String actionType, String operatorType, Long operatorId, String content) {
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderId(orderId);
        orderLog.setOrderStatus(orderStatus);
        orderLog.setActionType(actionType);
        orderLog.setOperatorType(operatorType);
        orderLog.setOperatorId(operatorId);
        orderLog.setContent(content);
        orderLogMapper.insert(orderLog);
    }

    public List<OrderLogResponse> listLogs(Long orderId) {
        return orderLogMapper.selectByOrderId(orderId).stream()
                .map(log -> OrderLogResponse.builder()
                        .orderStatus(log.getOrderStatus())
                        .actionType(log.getActionType())
                        .operatorType(log.getOperatorType())
                        .operatorId(log.getOperatorId())
                        .content(log.getContent())
                        .createdAt(log.getCreatedAt())
                        .build())
                .toList();
    }

    public void initPendingPayment(Order order, String payChannel) {
        PaymentRecord existed = paymentRecordMapper.selectByOrderId(order.getId());
        if (existed != null) {
            return;
        }
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setOrderId(order.getId());
        paymentRecord.setOrderNo(order.getOrderNo());
        paymentRecord.setPayAmount(order.getPayAmount());
        paymentRecord.setPayChannel(payChannel);
        paymentRecord.setPayStatus("PENDING");
        paymentRecordMapper.insert(paymentRecord);
    }

    public void markPaymentSuccess(Order order, String payChannel, String transactionNo) {
        PaymentRecord existed = paymentRecordMapper.selectByOrderId(order.getId());
        if (existed == null) {
            PaymentRecord paymentRecord = new PaymentRecord();
            paymentRecord.setOrderId(order.getId());
            paymentRecord.setOrderNo(order.getOrderNo());
            paymentRecord.setPayAmount(order.getPayAmount());
            paymentRecord.setPayChannel(payChannel);
            paymentRecord.setPayStatus("SUCCESS");
            paymentRecord.setTransactionNo(transactionNo);
            paymentRecord.setPaidAt(LocalDateTime.now());
            paymentRecordMapper.insert(paymentRecord);
            return;
        }
        paymentRecordMapper.updateStatus(order.getId(), "SUCCESS", transactionNo, LocalDateTime.now());
    }

    public void markRefundSuccess(Order order, String transactionNo) {
        PaymentRecord existed = paymentRecordMapper.selectByOrderId(order.getId());
        if (existed == null) {
            PaymentRecord paymentRecord = new PaymentRecord();
            paymentRecord.setOrderId(order.getId());
            paymentRecord.setOrderNo(order.getOrderNo());
            paymentRecord.setPayAmount(order.getPayAmount());
            paymentRecord.setPayChannel("MOCK");
            paymentRecord.setPayStatus("REFUNDED");
            paymentRecord.setTransactionNo(transactionNo);
            paymentRecord.setPaidAt(LocalDateTime.now());
            paymentRecordMapper.insert(paymentRecord);
            return;
        }
        paymentRecordMapper.updateStatus(
                order.getId(),
                "REFUNDED",
                transactionNo,
                existed.getPaidAt() == null ? LocalDateTime.now() : existed.getPaidAt()
        );
    }
}
