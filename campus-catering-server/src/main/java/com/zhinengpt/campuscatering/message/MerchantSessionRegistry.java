package com.zhinengpt.campuscatering.message;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
public class MerchantSessionRegistry {

    private final Map<Long, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    public void bind(Long merchantId, WebSocketSession session) {
        sessionMap.put(merchantId, session);
    }

    public void unbind(Long merchantId) {
        sessionMap.remove(merchantId);
    }

    public void send(Long merchantId, String message) {
        WebSocketSession session = sessionMap.get(merchantId);
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            session.sendMessage(new TextMessage(message));
        } catch (IOException ignored) {
        }
    }
}
