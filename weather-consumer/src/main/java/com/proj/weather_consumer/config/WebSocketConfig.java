package com.proj.weather_consumer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Enables WebSocket message handling, backed by a message broker.
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker.
        // Messages with "/topic" prefix will be routed to clients subscribed to those topics.
        config.enableSimpleBroker("/topic");

        // Define a prefix for messages that are bound for methods annotated with @MessageMapping.
        // Messages from clients to the server should be prefixed with "/app".
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registers a WebSocket endpoint that clients will use to connect.
        // The ".withSockJS()" provides fallback options for browsers that don't support WebSockets.
        registry.addEndpoint("/ws").withSockJS();
    }
}
