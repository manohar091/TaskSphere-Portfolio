package com.tasksphere.dto.issue;

import lombok.Data;

@Data
public class CreateIssueRequest {
    private Long projectId;
    private Long sprintId;
    private String type;
    private String summary;
    private String description;
    private String priority;
    private Long assigneeId;
    private Long reporterId;
}