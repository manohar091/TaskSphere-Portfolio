package com.tasksphere.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * Comprehensive Performance and Health Monitoring Service
 * 
 * Features:
 * - Custom health indicators
 * - Performance metrics collection
 * - Business metrics tracking
 * - System resource monitoring
 * - Cache performance monitoring
 */
@Service
public class MonitoringService implements HealthIndicator {

    private final MeterRegistry meterRegistry;
    private final DataSource dataSource;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // Performance counters
    private final Counter taskCreatedCounter;
    private final Counter taskCompletedCounter;
    private final Counter userLoginCounter;
    private final Counter apiCallCounter;
    private final Counter errorCounter;
    
    // Performance gauges
    private final AtomicInteger activeUsersGauge = new AtomicInteger(0);
    private final AtomicInteger activeTasks = new AtomicInteger(0);
    private final AtomicInteger activeProjects = new AtomicInteger(0);
    private final AtomicLong totalMemoryUsage = new AtomicLong(0);
    
    // Performance timers
    private final Timer databaseQueryTimer;
    private final Timer cacheAccessTimer;
    private final Timer apiResponseTimer;

    @Autowired
    public MonitoringService(MeterRegistry meterRegistry, 
                           DataSource dataSource, 
                           RedisTemplate<String, Object> redisTemplate) {
        this.meterRegistry = meterRegistry;
        this.dataSource = dataSource;
        this.redisTemplate = redisTemplate;
        
        // Initialize counters
        this.taskCreatedCounter = Counter.builder("tasksphere.tasks.created")
                .description("Number of tasks created")
                .register(meterRegistry);
                
        this.taskCompletedCounter = Counter.builder("tasksphere.tasks.completed")
                .description("Number of tasks completed")
                .register(meterRegistry);
                
        this.userLoginCounter = Counter.builder("tasksphere.users.logins")
                .description("Number of user logins")
                .register(meterRegistry);
                
        this.apiCallCounter = Counter.builder("tasksphere.api.calls")
                .description("Number of API calls")
                .register(meterRegistry);
                
        this.errorCounter = Counter.builder("tasksphere.errors")
                .description("Number of errors")
                .register(meterRegistry);
        
        // Initialize gauges
        Gauge.builder("tasksphere.users.active", activeUsersGauge, AtomicInteger::doubleValue)
                .description("Number of active users")
                .register(meterRegistry);
                
        Gauge.builder("tasksphere.tasks.active", activeTasks, AtomicInteger::doubleValue)
                .description("Number of active tasks")
                .register(meterRegistry);
                
        Gauge.builder("tasksphere.projects.active", activeProjects, AtomicInteger::doubleValue)
                .description("Number of active projects")
                .register(meterRegistry);
                
        Gauge.builder("tasksphere.memory.usage", totalMemoryUsage, AtomicLong::doubleValue)
                .description("Memory usage in bytes")
                .register(meterRegistry);
        
        // Initialize timers
        this.databaseQueryTimer = Timer.builder("tasksphere.database.query.time")
                .description("Database query execution time")
                .register(meterRegistry);
                
        this.cacheAccessTimer = Timer.builder("tasksphere.cache.access.time")
                .description("Cache access time")
                .register(meterRegistry);
                
        this.apiResponseTimer = Timer.builder("tasksphere.api.response.time")
                .description("API response time")
                .register(meterRegistry);
    }

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        
        try {
            // Check database connectivity
            boolean dbHealthy = checkDatabaseHealth();
            details.put("database", dbHealthy ? "UP" : "DOWN");
            
            // Check Redis connectivity
            boolean redisHealthy = checkRedisHealth();
            details.put("redis", redisHealthy ? "UP" : "DOWN");
            
            // Check system resources
            Map<String, Object> systemHealth = checkSystemHealth();
            details.put("system", systemHealth);
            
            // Check application metrics
            Map<String, Object> appMetrics = getApplicationMetrics();
            details.put("metrics", appMetrics);
            
            // Overall health determination
            boolean overall = dbHealthy && redisHealthy;
            
            return overall ? 
                Health.up().withDetails(details).build() : 
                Health.down().withDetails(details).build();
                
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetails(details)
                    .build();
        }
    }

    private boolean checkDatabaseHealth() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5); // 5-second timeout
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean checkRedisHealth() {
        try {
            redisTemplate.opsForValue().set("health_check", "ping");
            String result = (String) redisTemplate.opsForValue().get("health_check");
            return "ping".equals(result);
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, Object> checkSystemHealth() {
        Map<String, Object> systemHealth = new HashMap<>();
        
        // Memory usage
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        systemHealth.put("memory.max", maxMemory);
        systemHealth.put("memory.total", totalMemory);
        systemHealth.put("memory.used", usedMemory);
        systemHealth.put("memory.free", freeMemory);
        systemHealth.put("memory.usage.percent", (double) usedMemory / maxMemory * 100);
        
        // Update memory gauge
        totalMemoryUsage.set(usedMemory);
        
        // CPU information
        systemHealth.put("cpu.processors", runtime.availableProcessors());
        
        // JVM information
        systemHealth.put("jvm.version", System.getProperty("java.version"));
        systemHealth.put("jvm.vendor", System.getProperty("java.vendor"));
        
        return systemHealth;
    }

    @Cacheable(value = "monitoring", key = "'app-metrics'")
    public Map<String, Object> getApplicationMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        metrics.put("tasks.created.total", taskCreatedCounter.count());
        metrics.put("tasks.completed.total", taskCompletedCounter.count());
        metrics.put("users.logins.total", userLoginCounter.count());
        metrics.put("api.calls.total", apiCallCounter.count());
        metrics.put("errors.total", errorCounter.count());
        
        metrics.put("users.active.current", activeUsersGauge.get());
        metrics.put("tasks.active.current", activeTasks.get());
        metrics.put("projects.active.current", activeProjects.get());
        
        metrics.put("timestamp", LocalDateTime.now().toString());
        
        return metrics;
    }

    // Business metrics methods
    public void recordTaskCreated() {
        taskCreatedCounter.increment();
        activeTasks.incrementAndGet();
    }

    public void recordTaskCompleted() {
        taskCompletedCounter.increment();
        activeTasks.decrementAndGet();
    }

    public void recordUserLogin() {
        userLoginCounter.increment();
    }

    public void recordApiCall() {
        apiCallCounter.increment();
    }

    public void recordError() {
        errorCounter.increment();
    }

    public void setActiveUsers(int count) {
        activeUsersGauge.set(count);
    }

    public void setActiveProjects(int count) {
        activeProjects.set(count);
    }

    // Performance timing methods
    public Timer.Sample startDatabaseTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopDatabaseTimer(Timer.Sample sample) {
        sample.stop(databaseQueryTimer);
    }

    public Timer.Sample startCacheTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopCacheTimer(Timer.Sample sample) {
        sample.stop(cacheAccessTimer);
    }

    public Timer.Sample startApiTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopApiTimer(Timer.Sample sample) {
        sample.stop(apiResponseTimer);
    }

    @Cacheable(value = "monitoring", key = "'performance-stats'")
    public Map<String, Object> getPerformanceStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Database performance
        stats.put("database.query.count", databaseQueryTimer.count());
        stats.put("database.query.avg.time", databaseQueryTimer.mean(TimeUnit.MILLISECONDS));
        stats.put("database.query.max.time", databaseQueryTimer.max(TimeUnit.MILLISECONDS));
        
        // Cache performance
        stats.put("cache.access.count", cacheAccessTimer.count());
        stats.put("cache.access.avg.time", cacheAccessTimer.mean(TimeUnit.MILLISECONDS));
        stats.put("cache.access.max.time", cacheAccessTimer.max(TimeUnit.MILLISECONDS));
        
        // API performance
        stats.put("api.response.count", apiResponseTimer.count());
        stats.put("api.response.avg.time", apiResponseTimer.mean(TimeUnit.MILLISECONDS));
        stats.put("api.response.max.time", apiResponseTimer.max(TimeUnit.MILLISECONDS));
        
        return stats;
    }

    public Map<String, Object> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        
        // Application info
        info.put("application.name", "TaskSphere");
        info.put("application.version", "1.0.0");
        info.put("spring.boot.version", org.springframework.boot.SpringBootVersion.getVersion());
        
        // System info
        info.put("os.name", System.getProperty("os.name"));
        info.put("os.version", System.getProperty("os.version"));
        info.put("os.arch", System.getProperty("os.arch"));
        
        // Java info
        info.put("java.version", System.getProperty("java.version"));
        info.put("java.vendor", System.getProperty("java.vendor"));
        info.put("java.home", System.getProperty("java.home"));
        
        return info;
    }
}