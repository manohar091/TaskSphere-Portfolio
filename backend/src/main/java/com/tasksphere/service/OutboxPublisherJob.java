package com.tasksphere.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tasksphere.domain.outbox.OutboxEvent;
import com.tasksphere.domain.outbox.OutboxEventRepository;
import com.tasksphere.event.RealtimeEvent;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Background job that publishes events from the outbox to Redis
 * Ensures reliable delivery using the outbox pattern
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherJob {

    private final OutboxEventRepository outbox;
    private final RealtimePublisher publisher;
    private final MeterRegistry meterRegistry;

    private Counter publishedCounter;
    private Counter failedCounter;

    @PostConstruct
    void init() {
        publishedCounter = Counter.builder("realtime.events.published")
                .description("Successfully published events from outbox")
                .register(meterRegistry);
        failedCounter = Counter.builder("realtime.events.failed")
                .description("Failed to publish events from outbox")
                .register(meterRegistry);
    }

    @Scheduled(fixedDelay = 1000) // every 1 second
    @Transactional
    public void publishBatch() {
        List<OutboxEvent> batch = outbox.findTop100ByPublishedFalseOrderByCreatedAtAsc();
        if (batch.isEmpty()) {
            return;
        }
        
        log.debug("Processing {} outbox events", batch.size());
        
        for (OutboxEvent e : batch) {
            try {
                RealtimeEvent evt = new RealtimeEvent(
                    e.getEventId(),
                    e.getType(),
                    parseProjectId(e.getChannel()),
                    "System"
                );
                
                evt.setIssueId(parseIssueId(e.getChannel()));
                evt.setSprintId(parseSprintId(e.getChannel()));
                evt.setPayload(e.getPayload());
                
                publisher.publish(e.getChannel(), evt);
                e.setPublished(true);
                publishedCounter.increment();
                
                log.debug("Published outbox event: {} to channel: {}", e.getType(), e.getChannel());
                
            } catch (Exception ex) {
                failedCounter.increment();
                log.error("Outbox publish failed for event id={}, type={}", e.getId(), e.getType(), ex);
            }
        }
        
        // Save the published flag updates
        outbox.saveAll(batch);
    }

    private Long parseProjectId(String channel) {
        // channel like "project.12" or "issue.101"
        try {
            if (channel.startsWith("project.")) {
                return Long.valueOf(channel.substring(8));
            }
            // For issue/sprint channels, we'd need to look up the project ID
            // For now, return null and let the event specify it
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long parseIssueId(String channel) {
        try {
            if (channel.startsWith("issue.")) {
                return Long.valueOf(channel.substring(6));
            }
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long parseSprintId(String channel) {
        try {
            if (channel.startsWith("sprint.")) {
                return Long.valueOf(channel.substring(7));
            }
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}