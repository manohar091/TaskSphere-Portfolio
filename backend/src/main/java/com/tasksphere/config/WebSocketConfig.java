package com.tasksphere.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.tasksphere.security.WebSocketAuthInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor authInterceptor;

    @Bean
    @NonNull
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("websocket-");
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
        // Enable a simple in-memory broker with heartbeats for production reliability
        registry.enableSimpleBroker("/topic")
                .setHeartbeatValue(new long[]{10_000L, 10_000L}) // server<->client 10s heartbeats
                .setTaskScheduler(taskScheduler()); // Add TaskScheduler for heartbeats
        registry.setApplicationDestinationPrefixes("/app"); // incoming from client
        registry.setUserDestinationPrefix("/user"); // private user messages
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")                 // websocket endpoint
                .addInterceptors(authInterceptor)   // Add JWT authentication
                .setAllowedOrigins(
                    "http://localhost:3000",
                    "http://localhost:3001", 
                    "https://tasksphere.app",
                    "https://www.tasksphere.app"
                )
                .withSockJS();                      // SockJS fallback support
    }
}