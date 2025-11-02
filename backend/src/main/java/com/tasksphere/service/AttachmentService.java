package com.tasksphere.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tasksphere.domain.attachment.Attachment;
import com.tasksphere.domain.attachment.AttachmentRepository;
import com.tasksphere.domain.issue.IssueRepository;
import com.tasksphere.dto.attachment.AttachmentView;
import com.tasksphere.dto.attachment.PresignPutRequest;
import com.tasksphere.dto.attachment.PresignPutResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttachmentService {
    private final AttachmentRepository repo;
    private final IssueRepository issues;

    @Value("${app.s3.bucket:tasksphere-dev-bucket}") 
    private String bucket;

    public PresignPutResponse presignUpload(PresignPutRequest req) {
        String key = "issues/" + req.getIssueId() + "/" + UUID.randomUUID() + "_" + req.getFilename();
        
        // Mock presigned URL for development
        String mockUploadUrl = "https://" + bucket + ".s3.amazonaws.com/" + key + "?mock=true";

        Attachment a = new Attachment();
        a.setIssue(issues.findById(req.getIssueId()).orElseThrow());
        a.setS3Key(key);
        a.setFilename(req.getFilename());
        a.setSize(req.getSize());
        a.setContentType(req.getContentType());
        // Note: uploader field needs to be set, but we don't have auth context in this simplified version
        repo.save(a);

        return new PresignPutResponse(mockUploadUrl, key, a.getId());
    }

    public AttachmentView get(Long id) {
        Attachment a = repo.findById(id).orElseThrow();
        
        // Mock download URL for development
        String mockDownloadUrl = "https://" + bucket + ".s3.amazonaws.com/" + a.getS3Key() + "?mock=download";
        
        return new AttachmentView(a.getId(), a.getFilename(), a.getSize(),
                a.getContentType(), mockDownloadUrl);
    }
}