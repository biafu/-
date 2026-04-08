package com.zhinengpt.campuscatering.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhinengpt.campuscatering.order.dto.OrderWsMessage;
import org.springframework.stereotype.Component;

@Component
public class OrderNotifier {

    private final MerchantSessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper;

    public OrderNotifier(MerchantSessionRegistry sessionRegistry, ObjectMapper objectMapper) {
        this.sessionRegistry = sessionRegistry;
        this.objectMapper = objectMapper;
    }

    public void notifyMerchant(OrderWsMessage message) {
        try {
            sessionRegistry.send(message.getMerchantId(), objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException ignored) {
        }
    }
}
