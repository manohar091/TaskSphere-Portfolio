package com.tasksphere.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tasksphere.dto.activity.ActivityView;
import com.tasksphere.service.ActivityService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {
    private final ActivityService service;

    @GetMapping
    public List<ActivityView> list(@RequestParam String entityType,
                                   @RequestParam Long entityId) {
        return service.list(entityType, entityId);
    }
}