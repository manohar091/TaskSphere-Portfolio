package com.tasksphere.controller;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", "TaskSphere");
        response.put("timestamp", System.currentTimeMillis());
        
        // Test database connectivity
        try (Connection connection = dataSource.getConnection()) {
            response.put("database", "CONNECTED");
            response.put("database_url", connection.getMetaData().getURL());
        } catch (Exception e) {
            response.put("database", "DISCONNECTED");
            response.put("database_error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}