package com.tasksphere.dto.attachment;

import lombok.Data;

@Data
public class PresignPutRequest {
    private Long issueId;
    private String filename;
    private String contentType;
    private Long size;
}