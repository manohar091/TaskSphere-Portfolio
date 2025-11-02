package com.tasksphere.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * 📊 Custom Metrics Configuration
 * Provides business metrics for TaskSphere application monitoring
 */
@Configuration
@EnableScheduling
public class MetricsConfig {

    private final MeterRegistry meterRegistry;
    private final DataSource dataSource;
    
    // Metrics counters and gauges
    private final AtomicLong activeUsers = new AtomicLong(0);
    private final AtomicLong totalTasks = new AtomicLong(0);
    private final AtomicLong totalProjects = new AtomicLong(0);
    private final AtomicLong databaseConnections = new AtomicLong(0);
    
    public MetricsConfig(MeterRegistry meterRegistry, DataSource dataSource) {
        this.meterRegistry = meterRegistry;
        this.dataSource = dataSource;
        initializeMetrics();
    }

    /**
     * Initialize custom metrics
     */
    private void initializeMetrics() {
        // Gauge for active users (last 5 minutes)
        Gauge.builder("tasksphere.users.active", this, obj -> obj.activeUsers.get())
                .description("Number of active users in the last 5 minutes")
                .register(meterRegistry);

        // Gauge for total tasks
        Gauge.builder("tasksphere.tasks.total", this, obj -> obj.totalTasks.get())
                .description("Total number of tasks in the system")
                .register(meterRegistry);

        // Gauge for total projects
        Gauge.builder("tasksphere.projects.total", this, obj -> obj.totalProjects.get())
                .description("Total number of projects in the system")
                .register(meterRegistry);

        // Gauge for database connections
        Gauge.builder("tasksphere.database.connections", this, obj -> obj.databaseConnections.get())
                .description("Number of active database connections")
                .register(meterRegistry);
    }

    /**
     * Counter for task creation events
     */
    @Bean
    public Counter taskCreationCounter() {
        return Counter.builder("tasksphere.tasks.created")
                .description("Number of tasks created")
                .tag("type", "business")
                .register(meterRegistry);
    }

    /**
     * Counter for project creation events
     */
    @Bean
    public Counter projectCreationCounter() {
        return Counter.builder("tasksphere.projects.created")
                .description("Number of projects created")
                .tag("type", "business")
                .register(meterRegistry);
    }

    /**
     * Counter for user login events
     */
    @Bean
    public Counter userLoginCounter() {
        return Counter.builder("tasksphere.users.logins")
                .description("Number of user login events")
                .tag("type", "security")
                .register(meterRegistry);
    }

    /**
     * Timer for API response times
     */
    @Bean
    public Timer apiResponseTimer() {
        return Timer.builder("tasksphere.api.response.time")
                .description("API response time")
                .tag("type", "performance")
                .register(meterRegistry);
    }

    /**
     * Counter for API errors
     */
    @Bean
    public Counter apiErrorCounter() {
        return Counter.builder("tasksphere.api.errors")
                .description("Number of API errors")
                .tag("type", "error")
                .register(meterRegistry);
    }

    /**
     * Update metrics every minute
     */
    @Scheduled(fixedRate = 60000) // Every minute
    public void updateMetrics() {
        try {
            updateActiveUsers();
            updateTotalCounts();
            updateDatabaseConnections();
        } catch (Exception e) {
            System.err.println("Error updating metrics: " + e.getMessage());
        }
    }

    /**
     * Update active users count
     */
    private void updateActiveUsers() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Count users active in last 5 minutes (assuming we have user session tracking)
            String sql = "SELECT COUNT(DISTINCT user_id) as active_count " +
                        "FROM user_sessions " +
                        "WHERE last_activity > DATE_SUB(NOW(), INTERVAL 5 MINUTE)";
            
            try (ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    activeUsers.set(rs.getLong("active_count"));
                }
            }
        } catch (Exception e) {
            // Table might not exist yet, ignore error
            activeUsers.set(0);
        }
    }

    /**
     * Update total counts for tasks and projects
     */
    private void updateTotalCounts() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Count total tasks
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM tasks")) {
                if (rs.next()) {
                    totalTasks.set(rs.getLong("count"));
                }
            } catch (Exception e) {
                totalTasks.set(0);
            }
            
            // Count total projects
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM projects")) {
                if (rs.next()) {
                    totalProjects.set(rs.getLong("count"));
                }
            } catch (Exception e) {
                totalProjects.set(0);
            }
        } catch (Exception e) {
            // Database not available, keep existing values
        }
    }

    /**
     * Update database connection count
     */
    private void updateDatabaseConnections() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Get MySQL connection count
            String sql = "SHOW STATUS LIKE 'Threads_connected'";
            try (ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    databaseConnections.set(Long.parseLong(rs.getString("Value")));
                }
            }
        } catch (Exception e) {
            // Keep existing value on error
        }
    }
}