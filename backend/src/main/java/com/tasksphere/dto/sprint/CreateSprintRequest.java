package com.tasksphere.dto.sprint;

import java.time.LocalDate;

import lombok.Data;

@Data
public class CreateSprintRequest {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
}