package com.tasksphere.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tasksphere.domain.issue.Issue;
import com.tasksphere.domain.issue.IssueRepository;
import com.tasksphere.domain.project.ProjectRepository;
import com.tasksphere.domain.sprint.SprintRepository;
import com.tasksphere.domain.user.UserRepository;
import com.tasksphere.dto.issue.CreateIssueRequest;
import com.tasksphere.dto.issue.IssueView;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssueService {
    private final IssueRepository issues;
    private final ProjectRepository projects;
    private final SprintRepository sprints;
    private final UserRepository users;

    public IssueView create(CreateIssueRequest req) {
        Issue i = new Issue();
        i.setProject(projects.findById(req.getProjectId()).orElseThrow(() -> 
            new RuntimeException("Project not found")));
        if (req.getSprintId() != null)
            i.setSprint(sprints.findById(req.getSprintId()).orElse(null));
        i.setType(req.getType());
        i.setStatus("TODO");
        i.setPriority(req.getPriority());
        i.setSummary(req.getSummary());
        i.setDescription(req.getDescription());
        i.setReporter(users.findById(req.getReporterId()).orElseThrow(() -> 
            new RuntimeException("Reporter not found")));
        if (req.getAssigneeId() != null)
            i.setAssignee(users.findById(req.getAssigneeId()).orElse(null));
        issues.save(i);
        return toView(i);
    }

    public List<IssueView> list(Long projectId, String status) {
        return issues.findByProjectIdAndStatusOptional(projectId, status)
                .stream().map(this::toView).toList();
    }

    private IssueView toView(Issue i) {
        String assignee = i.getAssignee() != null ? i.getAssignee().getName() : "-";
        return new IssueView(i.getId(), i.getType(), i.getStatus(),
                i.getPriority(), i.getSummary(), assignee, i.getReporter().getName());
    }
}