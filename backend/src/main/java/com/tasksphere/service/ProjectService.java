package com.tasksphere.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tasksphere.domain.project.Project;
import com.tasksphere.domain.project.ProjectRepository;
import com.tasksphere.domain.user.UserRepository;
import com.tasksphere.dto.project.CreateProjectRequest;
import com.tasksphere.dto.project.ProjectView;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projects;
    private final UserRepository users;
    private final OutboxService outboxService; // Use outbox for reliable delivery

    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or #req.ownerId == authentication.principal.id")
    @Transactional
    public ProjectView create(CreateProjectRequest req) {
        Project p = new Project();
        p.setKey(req.getKey());
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        Long ownerId = req.getOwnerId();
        if (ownerId != null) {
            p.setOwner(users.findById(ownerId).orElseThrow(() -> 
                new RuntimeException("Owner not found")));
        }
        projects.save(p);
        
        // Publish real-time event using outbox pattern
        String payload = String.format("{\"projectId\":%d,\"name\":\"%s\",\"key\":\"%s\"}", 
                                      p.getId(), p.getName(), p.getKey());
        outboxService.publishProjectEvent(p.getId(), "project.created", payload);
        
        return toView(p);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<ProjectView> list() {
        return projects.findAll().stream().map(this::toView).toList();
    }

    @PreAuthorize("@perm.canAccessProject(#id, authentication)")
    public Optional<ProjectView> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return projects.findById(id).map(this::toView);
    }

    @PreAuthorize("@perm.canManageProject(#id, authentication)")
    @Transactional
    public void deleteProject(Long id) {
        if (id == null) {
            return;
        }
        
        // Get project details before deletion for event
        Optional<Project> project = projects.findById(id);
        projects.deleteById(id);
        
        // Publish real-time event using outbox pattern
        if (project.isPresent()) {
            String payload = String.format("{\"projectId\":%d,\"name\":\"%s\"}", 
                                          id, project.get().getName());
            outboxService.publishProjectEvent(id, "project.deleted", payload);
        }
    }

    private ProjectView toView(Project p) {
        return new ProjectView(p.getId(), p.getKey(), p.getName(), 
            p.getDescription(), p.getOwner().getName());
    }
}