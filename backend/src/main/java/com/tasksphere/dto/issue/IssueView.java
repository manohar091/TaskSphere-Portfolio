package com.tasksphere.dto.issue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssueView {
    private Long id;
    private String type;
    private String status;
    private String priority;
    private String summary;
    private String assigneeName;
    private String reporterName;
}