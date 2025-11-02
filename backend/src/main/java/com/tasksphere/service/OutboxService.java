package com.tasksphere.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tasksphere.domain.outbox.OutboxEvent;
import com.tasksphere.domain.outbox.OutboxEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for publishing events to the outbox for reliable delivery
 * Uses the outbox pattern to ensure events are published even if Redis is temporarily unavailable
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxEventRepository outboxRepository;

    /**
     * Publish an event to the outbox within the current transaction
     * This ensures the event is persisted atomically with business state changes
     */
    @Transactional
    public void publishEvent(String type, String channel, String payload) {
        OutboxEvent event = new OutboxEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setType(type);
        event.setChannel(channel);
        event.setPayload(payload != null ? payload : "{}");
        event.setPublished(false);
        
        outboxRepository.save(event);
        log.debug("Saved event to outbox: type={}, channel={}", type, channel);
    }

    /**
     * Publish a project-related event
     */
    @Transactional
    public void publishProjectEvent(Long projectId, String type, String payload) {
        publishEvent(type, "project." + projectId, payload);
    }

    /**
     * Publish an issue-related event
     */
    @Transactional
    public void publishIssueEvent(Long projectId, Long issueId, String type, String payload) {
        // Publish to both project and issue channels
        publishEvent(type, "project." + projectId, payload);
        publishEvent(type, "issue." + issueId, payload);
    }

    /**
     * Publish a sprint-related event
     */
    @Transactional
    public void publishSprintEvent(Long projectId, Long sprintId, String type, String payload) {
        // Publish to both project and sprint channels
        publishEvent(type, "project." + projectId, payload);
        publishEvent(type, "sprint." + sprintId, payload);
    }
}