package com.tasksphere.domain.issue;

import java.sql.Timestamp;

import com.tasksphere.domain.project.Project;
import com.tasksphere.domain.sprint.Sprint;
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
@Table(name="issues")
@Getter @Setter
public class Issue {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="project_id", nullable=false)
    private Project project;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="sprint_id")
    private Sprint sprint;

    @Column(nullable=false, length=16)
    private String type; // STORY|TASK|BUG|EPIC

    @Column(nullable=false, length=20)
    private String status; // BACKLOG|TODO|IN_PROGRESS|QA_READY|DONE

    @Column(nullable=false, length=16)
    private String priority; // LOW|MEDIUM|HIGH|CRITICAL

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="assignee_id")
    private User assignee;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="reporter_id", nullable=false)
    private User reporter;

    @Column(nullable=false, length=255)
    private String summary;

    @Column(columnDefinition="text")
    private String description;

    @Column(name="story_points")
    private Integer storyPoints;

    @Column(name="created_at", updatable=false, insertable=false)
    private Timestamp createdAt;

    @Column(name="updated_at", insertable=false)
    private Timestamp updatedAt;
}