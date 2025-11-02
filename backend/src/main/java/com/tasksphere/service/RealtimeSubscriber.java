package com.tasksphere.service;

import java.nio.charset.StandardCharsets;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis subscriber service for receiving and forwarding real-time events
 * Listens to Redis channels and forwards events to WebSocket clients
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RealtimeSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final MeterRegistry meterRegistry;
    
    private Counter deliveredCounter;

    @PostConstruct
    public void init() {
        deliveredCounter = Counter.builder("realtime.events.delivered")
                .description("Events delivered to WebSocket clients")
                .register(meterRegistry);
    }

    @Override
    public void onMessage(@NonNull Message message, @Nullable byte[] pattern) {
        try {
            // Extract channel name from pattern
            String channel = pattern != null ? new String(pattern, StandardCharsets.UTF_8) : "unknown";
            
            // Using GenericJackson2JsonRedisSerializer, the message body is already JSON
            String payload = new String(message.getBody(), StandardCharsets.UTF_8);
            
            // Forward to WebSocket topic
            String topic = "/topic/" + channel;
            messagingTemplate.convertAndSend(topic, payload);
            
            deliveredCounter.increment();
            log.debug("Forwarded event from Redis channel {} to WebSocket topic {}", channel, topic);
            
        } catch (Exception e) {
            log.error("Failed to process Redis message: {}", e.getMessage(), e);
        }
    }
}