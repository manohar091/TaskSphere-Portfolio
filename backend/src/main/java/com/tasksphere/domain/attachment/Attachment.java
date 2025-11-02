package com.tasksphere.domain.attachment;

import java.sql.Timestamp;

import com.tasksphere.domain.issue.Issue;
import com.tasksphere.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "attachments")
@Getter @Setter
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id")
    private User uploader;

    @Column(name = "filename", nullable = false, length = 255)
    private String filename;

    @Column(name = "s3_key", nullable = false, length = 512)
    private String s3Key;

    @Column(name = "content_type", length = 120)
    private String contentType;

    @Column(name = "size")
    private Long size;

    @Column(name = "uploaded_at", nullable = false, updatable = false, insertable = false)
    private Timestamp uploadedAt;
}