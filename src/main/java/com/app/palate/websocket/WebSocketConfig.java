package com.app.palate.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override

    @SuppressWarnings("null")
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/api/palate/ws") // WebSocket endpoint
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @SuppressWarnings("null")
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic"); // Clients subscribe here
        registry.setApplicationDestinationPrefixes("/app");
    }
}