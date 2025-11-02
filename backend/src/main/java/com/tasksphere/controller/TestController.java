package com.tasksphere.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tasksphere.event.RealtimeEvent;
import com.tasksphere.service.RealtimePublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Test controller for triggering real-time events
 * Used for testing WebSocket functionality
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final RealtimePublisher realtimePublisher;

    @PostMapping("/event")
    public ResponseEntity<String> triggerTestEvent(@RequestBody TestEventRequest request) {
        try {
            RealtimeEvent event = new RealtimeEvent(
                request.getType(), 
                request.getProjectId(), 
                request.getActor() != null ? request.getActor() : "TestUser"
            );
            
            event.setIssueId(request.getIssueId());
            event.setSprintId(request.getSprintId());
            event.setPayload(request.getMessage());
            
            // Publish based on event type
            if (request.getType().startsWith("project.")) {
                realtimePublisher.publishProjectEvent(request.getProjectId(), event);
            } else if (request.getType().startsWith("issue.")) {
                Long projectId = request.getProjectId() != null ? request.getProjectId() : 1L;
                Long issueId = request.getIssueId() != null ? request.getIssueId() : 1L;
                realtimePublisher.publishIssueEvent(projectId, issueId, event);
            } else if (request.getType().startsWith("sprint.")) {
                Long projectId = request.getProjectId() != null ? request.getProjectId() : 1L;
                Long sprintId = request.getSprintId() != null ? request.getSprintId() : 1L;
                realtimePublisher.publishSprintEvent(projectId, sprintId, event);
            } else {
                // Generic publish to direct channel
                realtimePublisher.publish("test.events", event);
            }
            
            log.info("Published test event: {}", request.getType());
            return ResponseEntity.ok("Event published successfully");
            
        } catch (Exception e) {
            log.error("Failed to publish test event", e);
            return ResponseEntity.internalServerError().body("Failed to publish event: " + e.getMessage());
        }
    }

    public static class TestEventRequest {
        private String type;
        private Long projectId;
        private Long issueId;
        private Long sprintId;
        private String actor;
        private String message;

        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        
        public Long getIssueId() { return issueId; }
        public void setIssueId(Long issueId) { this.issueId = issueId; }
        
        public Long getSprintId() { return sprintId; }
        public void setSprintId(Long sprintId) { this.sprintId = sprintId; }
        
        public String getActor() { return actor; }
        public void setActor(String actor) { this.actor = actor; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}