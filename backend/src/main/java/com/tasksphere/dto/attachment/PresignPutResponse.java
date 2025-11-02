package com.tasksphere.dto.attachment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PresignPutResponse {
    private String uploadUrl;
    private String s3Key;
    private Long attachmentId;
}