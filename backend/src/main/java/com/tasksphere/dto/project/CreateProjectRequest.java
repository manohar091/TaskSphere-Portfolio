package com.tasksphere.dto.project;

import lombok.Data;

@Data
public class CreateProjectRequest {
    private String key;
    private String name;
    private String description;
    private Long ownerId;
}