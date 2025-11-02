package com.tasksphere.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 📊 Performance Statistics Service with Redis Caching
 * 
 * This service demonstrates Redis caching implementation for
 * performance-critical operations in TaskSphere.
 */
@Service
public class PerformanceStatsService {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceStatsService.class);

    /**
     * 📈 Get Application Performance Statistics (Cached)
     * Cache key: "stats:app-performance"
     * TTL: 15 minutes (configured in cache manager)
     */
    @Cacheable(value = "statistics", key = "'app-performance'")
    public AppPerformanceStats getApplicationPerformanceStats() {
        logger.info("🔄 Computing application performance statistics (not from cache)");
        
        // Simulate expensive computation
        try {
            Thread.sleep(1000); // Simulate 1-second computation
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return new AppPerformanceStats(
            System.currentTimeMillis(),
            Runtime.getRuntime().totalMemory(),
            Runtime.getRuntime().freeMemory(),
            Runtime.getRuntime().maxMemory(),
            Runtime.getRuntime().availableProcessors(),
            "UP"
        );
    }

    /**
     * 📊 Get Database Performance Statistics (Cached)
     * Cache key: "stats:db-performance"
     * TTL: 15 minutes
     */
    @Cacheable(value = "statistics", key = "'db-performance'")
    public DatabasePerformanceStats getDatabasePerformanceStats() {
        logger.info("🔄 Computing database performance statistics (not from cache)");
        
        // Simulate database metrics computation
        try {
            Thread.sleep(500); // Simulate 500ms computation
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return new DatabasePerformanceStats(
            System.currentTimeMillis(),
            10, // Active connections
            20, // Max connections
            50.5, // Average response time
            99.9  // Uptime percentage
        );
    }

    /**
     * 🗑️ Clear Performance Statistics Cache
     * Use this when you want to force fresh computation
     */
    @CacheEvict(value = "statistics", allEntries = true)
    public void clearPerformanceStatsCache() {
        logger.info("🧹 Cleared all performance statistics cache");
    }

    /**
     * 🗑️ Clear Specific Cache Entry
     */
    @CacheEvict(value = "statistics", key = "'app-performance'")
    public void clearAppPerformanceStatsCache() {
        logger.info("🧹 Cleared application performance statistics cache");
    }

    // Data Transfer Objects for Performance Statistics
    
    public static class AppPerformanceStats {
        private final long timestamp;
        private final long totalMemory;
        private final long freeMemory;
        private final long maxMemory;
        private final int processors;
        private final String status;

        public AppPerformanceStats(long timestamp, long totalMemory, long freeMemory, 
                                 long maxMemory, int processors, String status) {
            this.timestamp = timestamp;
            this.totalMemory = totalMemory;
            this.freeMemory = freeMemory;
            this.maxMemory = maxMemory;
            this.processors = processors;
            this.status = status;
        }

        // Getters
        public long getTimestamp() { return timestamp; }
        public long getTotalMemory() { return totalMemory; }
        public long getFreeMemory() { return freeMemory; }
        public long getMaxMemory() { return maxMemory; }
        public long getUsedMemory() { return totalMemory - freeMemory; }
        public double getMemoryUsagePercent() { 
            return (double) getUsedMemory() / totalMemory * 100; 
        }
        public int getProcessors() { return processors; }
        public String getStatus() { return status; }
    }

    public static class DatabasePerformanceStats {
        private final long timestamp;
        private final int activeConnections;
        private final int maxConnections;
        private final double avgResponseTime;
        private final double uptimePercent;

        public DatabasePerformanceStats(long timestamp, int activeConnections, 
                                      int maxConnections, double avgResponseTime, 
                                      double uptimePercent) {
            this.timestamp = timestamp;
            this.activeConnections = activeConnections;
            this.maxConnections = maxConnections;
            this.avgResponseTime = avgResponseTime;
            this.uptimePercent = uptimePercent;
        }

        // Getters
        public long getTimestamp() { return timestamp; }
        public int getActiveConnections() { return activeConnections; }
        public int getMaxConnections() { return maxConnections; }
        public double getConnectionUsagePercent() { 
            return (double) activeConnections / maxConnections * 100; 
        }
        public double getAvgResponseTime() { return avgResponseTime; }
        public double getUptimePercent() { return uptimePercent; }
    }
}