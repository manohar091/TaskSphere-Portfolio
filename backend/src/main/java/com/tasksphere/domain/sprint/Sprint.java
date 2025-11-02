package com.tasksphere.domain.sprint;

import java.sql.Timestamp;
import java.time.LocalDate;

import com.tasksphere.domain.project.Project;

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
@Table(name="sprints")
@Getter @Setter
public class Sprint {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="project_id", nullable=false)
    private Project project;

    @Column(nullable=false, length=120)
    private String name;

    @Column(name="start_date")
    private LocalDate startDate;

    @Column(name="end_date")
    private LocalDate endDate;

    @Column(nullable=false, length=16)
    private String state; // PLANNED|ACTIVE|CLOSED

    @Column(name="created_at", updatable=false, insertable=false)
    private Timestamp createdAt;
}