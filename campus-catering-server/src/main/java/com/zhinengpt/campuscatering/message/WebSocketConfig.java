package com.zhinengpt.campuscatering.message;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final MerchantOrderWebSocketHandler merchantOrderWebSocketHandler;

    public WebSocketConfig(MerchantOrderWebSocketHandler merchantOrderWebSocketHandler) {
        this.merchantOrderWebSocketHandler = merchantOrderWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(merchantOrderWebSocketHandler, "/ws/orders")
                .setAllowedOriginPatterns("*");
    }
}
