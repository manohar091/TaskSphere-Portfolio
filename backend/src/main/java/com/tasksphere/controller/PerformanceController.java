package com.tasksphere.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tasksphere.service.PerformanceStatsService;
import com.tasksphere.service.PerformanceStatsService.AppPerformanceStats;
import com.tasksphere.service.PerformanceStatsService.DatabasePerformanceStats;

/**
 * 📊 Performance Monitoring Controller
 * 
 * Provides endpoints for accessing cached performance statistics
 * and cache management operations.
 */
@RestController
@RequestMapping("/api/performance")
public class PerformanceController {

    @Autowired
    private PerformanceStatsService performanceStatsService;

    /**
     * 📈 Get Application Performance Statistics
     * This endpoint returns cached statistics for better performance
     */
    @GetMapping("/app-stats")
    public ResponseEntity<AppPerformanceStats> getAppPerformanceStats() {
        AppPerformanceStats stats = performanceStatsService.getApplicationPerformanceStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 🗄️ Get Database Performance Statistics
     * This endpoint returns cached database metrics
     */
    @GetMapping("/db-stats")
    public ResponseEntity<DatabasePerformanceStats> getDatabasePerformanceStats() {
        DatabasePerformanceStats stats = performanceStatsService.getDatabasePerformanceStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 🗑️ Clear All Performance Cache
     * Use this to force fresh computation of all statistics
     */
    @DeleteMapping("/cache/all")
    public ResponseEntity<String> clearAllCache() {
        performanceStatsService.clearPerformanceStatsCache();
        return ResponseEntity.ok("All performance statistics cache cleared successfully");
    }

    /**
     * 🗑️ Clear Application Performance Cache
     * Use this to force fresh computation of app statistics only
     */
    @DeleteMapping("/cache/app")
    public ResponseEntity<String> clearAppCache() {
        performanceStatsService.clearAppPerformanceStatsCache();
        return ResponseEntity.ok("Application performance statistics cache cleared successfully");
    }
}