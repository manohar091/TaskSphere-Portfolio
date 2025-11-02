package com.tasksphere.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tasksphere.dto.attachment.AttachmentView;
import com.tasksphere.dto.attachment.PresignPutRequest;
import com.tasksphere.dto.attachment.PresignPutResponse;
import com.tasksphere.service.AttachmentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
public class AttachmentController {
    private final AttachmentService service;

    @PostMapping("/presign")
    public PresignPutResponse presign(@RequestBody PresignPutRequest req) {
        return service.presignUpload(req);
    }

    @GetMapping("/{id}")
    public AttachmentView get(@PathVariable Long id) {
        return service.get(id);
    }
}