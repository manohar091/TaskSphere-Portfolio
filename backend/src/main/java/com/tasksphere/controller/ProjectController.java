package com.tasksphere.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tasksphere.dto.project.CreateProjectRequest;
import com.tasksphere.dto.project.ProjectView;
import com.tasksphere.service.ProjectService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService service;

    @PostMapping
    public ResponseEntity<ProjectView> create(@RequestBody CreateProjectRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @GetMapping
    public List<ProjectView> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectView> one(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}