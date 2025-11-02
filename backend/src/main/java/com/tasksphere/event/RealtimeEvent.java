package com.tasksphere.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Real-time event model for WebSocket communication
 * Used to broadcast project and issue updates to connected clients
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RealtimeEvent {
    
    /**
     * Unique identifier for idempotency (UUID)
     */
    private String eventId;
    
    /**
     * Event type (e.g., ISSUE_CREATED, ISSUE_UPDATED, PROJECT_UPDATED, COMMENT_ADDED)
     */
    private String type;
    
    /**
     * Project ID that this event relates to
     */
    private Long projectId;
    
    /**
     * Issue ID (optional, only for issue-related events)
     */
    private Long issueId;
    
    /**
     * Sprint ID (optional, only for sprint-related events)
     */
    private Long sprintId;
    
    /**
     * Actor who performed the action
     */
    private String actor;
    
    /**
     * Compact JSON payload with additional event details
     */
    private String payload;
    
    /**
     * Timestamp when the event occurred
     */
    private Long timestamp;
    
    public RealtimeEvent(String eventId, String type, Long projectId) {
        this.eventId = eventId;
        this.type = type;
        this.projectId = projectId;
        this.timestamp = System.currentTimeMillis();
    }
    
    public RealtimeEvent(String eventId, String type, Long projectId, String actor) {
        this.eventId = eventId;
        this.type = type;
        this.projectId = projectId;
        this.actor = actor;
        this.timestamp = System.currentTimeMillis();
    }
    
    public RealtimeEvent(String type, Long projectId) {
        this.type = type;
        this.projectId = projectId;
        this.timestamp = System.currentTimeMillis();
    }

    public RealtimeEvent(String type, Long projectId, String actor) {
        this.type = type;
        this.projectId = projectId;
        this.actor = actor;
        this.timestamp = System.currentTimeMillis();
    }
}