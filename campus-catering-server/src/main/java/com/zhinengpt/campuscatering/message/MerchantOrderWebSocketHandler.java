package com.zhinengpt.campuscatering.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhinengpt.campuscatering.common.enums.UserRole;
import com.zhinengpt.campuscatering.security.JwtTokenService;
import com.zhinengpt.campuscatering.security.LoginUser;
import java.net.URI;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class MerchantOrderWebSocketHandler extends TextWebSocketHandler {

    private final JwtTokenService jwtTokenService;
    private final MerchantSessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper;

    public MerchantOrderWebSocketHandler(JwtTokenService jwtTokenService,
                                         MerchantSessionRegistry sessionRegistry,
                                         ObjectMapper objectMapper) {
        this.jwtTokenService = jwtTokenService;
        this.sessionRegistry = sessionRegistry;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        URI uri = session.getUri();
        if (uri == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        String token = UriComponentsBuilder.fromUri(uri).build().getQueryParams().getFirst("token");
        LoginUser loginUser = jwtTokenService.parseToken(token);
        if (loginUser.getRole() != UserRole.MERCHANT || loginUser.getMerchantId() == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        session.getAttributes().put("merchantId", loginUser.getMerchantId());
        sessionRegistry.bind(loginUser.getMerchantId(), session);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                "event", "CONNECTED",
                "merchantId", loginUser.getMerchantId()
        ))));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        if ("ping".equalsIgnoreCase(message.getPayload())) {
            session.sendMessage(new TextMessage("pong"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Object merchantId = session.getAttributes().get("merchantId");
        if (merchantId instanceof Long id) {
            sessionRegistry.unbind(id);
        }
    }
}
