package com.tasksphere.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tasksphere.dto.issue.CreateIssueRequest;
import com.tasksphere.dto.issue.IssueView;
import com.tasksphere.service.IssueService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssueController {
    private final IssueService issues;

    @PostMapping
    public IssueView create(@RequestBody CreateIssueRequest req) {
        return issues.create(req);
    }

    @GetMapping
    public List<IssueView> list(@RequestParam Long projectId,
                                @RequestParam(required = false) String status) {
        return issues.list(projectId, status);
    }
}