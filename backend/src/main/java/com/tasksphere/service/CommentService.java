package com.tasksphere.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.tasksphere.domain.comment.Comment;
import com.tasksphere.domain.comment.CommentRepository;
import com.tasksphere.domain.issue.IssueRepository;
import com.tasksphere.domain.user.User;
import com.tasksphere.domain.user.UserRepository;
import com.tasksphere.dto.comment.CommentView;
import com.tasksphere.dto.comment.CreateCommentRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository repo;
    private final IssueRepository issues;
    private final UserRepository users;

    public List<CommentView> list(Long issueId) {
        return repo.findByIssueIdOrderByCreatedAtAsc(issueId)
                .stream().map(this::toView).toList();
    }

    public CommentView add(Long issueId, CreateCommentRequest req, Authentication auth) {
        User user = (User) auth.getPrincipal();
        Comment c = new Comment();
        c.setIssue(issues.findById(issueId).orElseThrow());
        c.setAuthor(user);
        c.setText(req.getText());
        repo.save(c);
        return toView(c);
    }

    private CommentView toView(Comment c) {
        return new CommentView(c.getId(), c.getAuthor().getName(),
                c.getText(), c.getCreatedAt());
    }
}