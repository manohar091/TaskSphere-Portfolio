package com.tasksphere.dto.project;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectView {
    private Long id;
    private String key;
    private String name;
    private String description;
    private String ownerName;
}