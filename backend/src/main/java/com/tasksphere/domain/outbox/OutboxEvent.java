package com.tasksphere.domain.outbox;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
public class OutboxEvent {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;
    
    @Column(name = "event_id", nullable = false, unique = true, length = 36) 
    private String eventId;
    
    @Column(nullable = false, length = 64) 
    private String type;
    
    @Column(nullable = false, length = 128) 
    private String channel;
    
    @Column(columnDefinition = "json", nullable = false) 
    private String payload;
    
    @Column(nullable = false) 
    private boolean published = false;
    
    @Column(name = "created_at", insertable = false, updatable = false) 
    private Timestamp createdAt;
}