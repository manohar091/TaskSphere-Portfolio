package com.tasksphere.dto.sprint;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SprintView {
    private Long id;
    private String name;
    private String state;
    private LocalDate startDate;
    private LocalDate endDate;
}