package com.tasksphere.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tasksphere.dto.comment.CommentView;
import com.tasksphere.dto.comment.CreateCommentRequest;
import com.tasksphere.service.CommentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/issues/{issueId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService comments;

    @GetMapping
    public List<CommentView> list(@PathVariable Long issueId) {
        return comments.list(issueId);
    }

    @PostMapping
    public CommentView add(@PathVariable Long issueId,
                           @RequestBody CreateCommentRequest req,
                           Authentication auth) {
        return comments.add(issueId, req, auth);
    }
}