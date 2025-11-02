package com.tasksphere.dto.attachment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentView {
    private Long id;
    private String filename;
    private Long size;
    private String contentType;
    private String downloadUrl;
}