package com.tasksphere.service;

import org.springframework.stereotype.Service;

import com.tasksphere.domain.project.ProjectRepository;
import com.tasksphere.domain.sprint.Sprint;
import com.tasksphere.domain.sprint.SprintRepository;
import com.tasksphere.dto.sprint.CreateSprintRequest;
import com.tasksphere.dto.sprint.SprintView;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SprintService {
    private final SprintRepository repo;
    private final ProjectRepository projects;

    public SprintView create(Long projectId, CreateSprintRequest req) {
        Sprint s = new Sprint();
        s.setProject(projects.findById(projectId).orElseThrow(() -> 
            new RuntimeException("Project not found")));
        s.setName(req.getName());
        s.setStartDate(req.getStartDate());
        s.setEndDate(req.getEndDate());
        s.setState("ACTIVE");
        repo.save(s);
        return new SprintView(s.getId(), s.getName(), s.getState(), s.getStartDate(), s.getEndDate());
    }

    public SprintView getActive(Long projectId) {
        Sprint s = repo.findByProjectIdAndState(projectId, "ACTIVE")
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active sprint"));
        return new SprintView(s.getId(), s.getName(), s.getState(), s.getStartDate(), s.getEndDate());
    }
}