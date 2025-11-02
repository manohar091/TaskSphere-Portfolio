package com.tasksphere.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.tasksphere.domain.project.ProjectRepository;
import com.tasksphere.domain.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AppRunner implements CommandLineRunner {
    private final ProjectRepository projects;
    private final UserRepository users;

    @Override 
    public void run(String... args) {
        projects.findByKey("TS").ifPresent(p ->
            System.out.println("✅ Project: " + p.getName() + " (id=" + p.getId() + ")"));
        System.out.println("✅ Users count: " + users.count());
    }
}