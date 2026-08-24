package com.devteria.chat.controller;

import java.time.Instant;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.stereotype.Component;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.devteria.chat.dto.request.IntrospectRequest;
import com.devteria.chat.entity.WebSocketSession;
import com.devteria.chat.service.IdentityService;
import com.devteria.chat.service.WebSocketService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SocketHandler {
    SocketIOServer server;
    IdentityService identityService;
    WebSocketService webSocketService;

    @OnConnect
    public void clientConnected(SocketIOClient client) {
        // Get token
        String token = client.getHandshakeData().getSingleUrlParam("token");

        // Verify token
        var introspectResponse = identityService.introspect(
                IntrospectRequest.builder().token(token).build());

        // If token invalid -> disconnect
        if (introspectResponse.isValid()) {
            log.info("Client connected: {}", client.getSessionId());

            // Persist web socket session
            WebSocketSession webSocketSession = WebSocketSession.builder()
                    .socketSessionId(client.getSessionId().toString())
                    .userName(introspectResponse.getUserName())
                    .createdAt(Instant.now())
                    .build();

            webSocketService.create(webSocketSession);

            log.info("WebSocketSession created with id: {}", webSocketSession.getId());
        } else {
            log.error("Client disconnected: {}", client.getSessionId());
            client.disconnect();
        }

        log.info("Client connected: {}, {}", client.getSessionId(), token);
    }

    @OnDisconnect
    public void clientDisconnected(SocketIOClient client) {
        log.info("Client disconnected: {}", client.getSessionId());
        webSocketService.deleteSession(client.getSessionId().toString());
    }

    @PostConstruct
    public void startServer() {
        server.start();
        server.addListeners(this);
        log.info("Socket Server started");
    }

    @PreDestroy
    public void stopServer() {
        server.stop();
        log.info("Socket Server stopped");
    }
}
