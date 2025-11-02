package com.tasksphere.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.tasksphere.event.RealtimeEvent;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis publisher service for broadcasting real-time events
 * Publishes events to Redis channels for distribution to WebSocket clients
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RealtimePublisher {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final MeterRegistry meterRegistry;
    
    private Timer publishTimer;
    
    @PostConstruct
    public void init() {
        publishTimer = Timer.builder("realtime.publish.timer")
                .description("Redis publish latency")
                .register(meterRegistry);
    }
    
    /**
     * Publish an event to a specific Redis channel with metrics
     * @param channel The Redis channel name
     * @param event The event to publish
     */
    public void publish(String channel, RealtimeEvent event) {
        if (channel == null || event == null) {
            log.warn("Attempted to publish null channel or event");
            return;
        }
        
        publishTimer.record(() -> {
            try {
                redisTemplate.convertAndSend(channel, event);
                log.debug("Published event {} to channel {}", event.getType(), channel);
            } catch (Exception e) {
                log.error("Failed to publish event {} to channel {}: {}", 
                         event.getType(), channel, e.getMessage());
            }
        });
    }
    
    /**
     * Publish a project-related event
     * @param projectId The project ID
     * @param event The event to publish
     */
    public void publishProjectEvent(Long projectId, RealtimeEvent event) {
        publish("project." + projectId, event);
    }
    
    /**
     * Publish an issue-related event
     * @param projectId The project ID
     * @param issueId The issue ID
     * @param event The event to publish
     */
    public void publishIssueEvent(Long projectId, Long issueId, RealtimeEvent event) {
        // Publish to both project and issue-specific channels
        publish("project." + projectId, event);
        publish("issue." + issueId, event);
    }
    
    /**
     * Publish a sprint-related event
     * @param projectId The project ID
     * @param sprintId The sprint ID
     * @param event The event to publish
     */
    public void publishSprintEvent(Long projectId, Long sprintId, RealtimeEvent event) {
        // Publish to both project and sprint-specific channels
        publish("project." + projectId, event);
        publish("sprint." + sprintId, event);
    }
}