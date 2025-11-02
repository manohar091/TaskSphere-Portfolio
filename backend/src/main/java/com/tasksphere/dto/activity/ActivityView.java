package com.tasksphere.dto.activity;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityView {
    private Long id;
    private String entityType;
    private Long entityId;
    private String actorName;
    private String action;
    private String fromValue;
    private String toValue;
    private Timestamp createdAt;
}