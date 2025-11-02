package com.tasksphere.domain.project;

import java.sql.Timestamp;

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
@Table(name="projects")
@Getter @Setter
public class Project {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="`key`", unique=true, nullable=false, length=16)
    private String key;

    @Column(nullable=false, length=200)
    private String name;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="owner_id", nullable=false)
    private User owner;

    @Column(columnDefinition="text")
    private String description;

    @Column(name="created_at", updatable=false, insertable=false)
    private Timestamp createdAt;

    @Column(name="updated_at", insertable=false)
    private Timestamp updatedAt;
}