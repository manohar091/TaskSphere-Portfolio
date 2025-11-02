package com.tasksphere.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tasksphere.dto.sprint.CreateSprintRequest;
import com.tasksphere.dto.sprint.SprintView;
import com.tasksphere.service.SprintService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/projects/{projectId}/sprints")
@RequiredArgsConstructor
public class SprintController {
    private final SprintService sprints;

    @PostMapping
    public SprintView create(@PathVariable Long projectId, @RequestBody CreateSprintRequest req) {
        return sprints.create(projectId, req);
    }

    @GetMapping("/active")
    public SprintView active(@PathVariable Long projectId) {
        return sprints.getActive(projectId);
    }
}