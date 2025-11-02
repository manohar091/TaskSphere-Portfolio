package com.tasksphere.security;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Simple in-memory rate limiter for brute-force protection
 * For production, consider using Redis-based rate limiting
 */
@Component
public class RateLimiter {
    
    private final Map<String, AttemptRecord> attempts = new ConcurrentHashMap<>();
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int MAX_API_REQUESTS = 100; // per minute
    private static final int LOCKOUT_MINUTES = 15;
    
    @Data
    static class AttemptRecord {
        private int count;
        private LocalDateTime firstAttempt;
        private LocalDateTime lastAttempt;
        private boolean locked;
        
        public AttemptRecord() {
            this.count = 1;
            this.firstAttempt = LocalDateTime.now();
            this.lastAttempt = LocalDateTime.now();
            this.locked = false;
        }
    }
    
    /**
     * Check login attempts for a specific email
     * @param email User email
     * @throws RuntimeException if too many attempts
     */
    public void checkLoginAttempts(String email) {
        AttemptRecord record = attempts.get(email);
        
        if (record == null) {
            attempts.put(email, new AttemptRecord());
            return;
        }
        
        // Check if account is locked
        if (record.isLocked()) {
            long minutesSinceLock = ChronoUnit.MINUTES.between(record.getLastAttempt(), LocalDateTime.now());
            if (minutesSinceLock < LOCKOUT_MINUTES) {
                throw new RuntimeException(
                    String.format("Account locked due to too many failed login attempts. Try again in %d minutes.", 
                    LOCKOUT_MINUTES - minutesSinceLock)
                );
            } else {
                // Unlock after timeout
                attempts.remove(email);
                attempts.put(email, new AttemptRecord());
                return;
            }
        }
        
        // Check if within rate limit window (last hour)
        long minutesSinceFirst = ChronoUnit.MINUTES.between(record.getFirstAttempt(), LocalDateTime.now());
        if (minutesSinceFirst > 60) {
            // Reset counter after 1 hour
            attempts.put(email, new AttemptRecord());
            return;
        }
        
        // Increment attempt count
        record.setCount(record.getCount() + 1);
        record.setLastAttempt(LocalDateTime.now());
        
        if (record.getCount() >= MAX_LOGIN_ATTEMPTS) {
            record.setLocked(true);
            throw new RuntimeException(
                String.format("Too many failed login attempts (%d). Account locked for %d minutes.", 
                MAX_LOGIN_ATTEMPTS, LOCKOUT_MINUTES)
            );
        }
    }
    
    /**
     * Check API rate limit for a specific identifier (IP or user)
     * @param identifier IP address or user ID
     * @throws RuntimeException if rate limit exceeded
     */
    public void checkApiRateLimit(String identifier) {
        String key = "api_" + identifier;
        AttemptRecord record = attempts.get(key);
        
        if (record == null) {
            attempts.put(key, new AttemptRecord());
            return;
        }
        
        // Check if within rate limit window (last minute)
        long minutesSinceFirst = ChronoUnit.MINUTES.between(record.getFirstAttempt(), LocalDateTime.now());
        if (minutesSinceFirst >= 1) {
            // Reset counter after 1 minute
            attempts.put(key, new AttemptRecord());
            return;
        }
        
        // Increment request count
        record.setCount(record.getCount() + 1);
        record.setLastAttempt(LocalDateTime.now());
        
        if (record.getCount() > MAX_API_REQUESTS) {
            throw new RuntimeException(
                String.format("API rate limit exceeded (%d requests per minute). Please slow down.", MAX_API_REQUESTS)
            );
        }
    }
    
    /**
     * Reset attempts for successful login
     * @param email User email
     */
    public void resetLoginAttempts(String email) {
        attempts.remove(email);
    }
    
    /**
     * Get current attempt count for an identifier
     * @param identifier Email or API identifier
     * @return current attempt count
     */
    public int getCurrentAttempts(String identifier) {
        AttemptRecord record = attempts.get(identifier);
        return record != null ? record.getCount() : 0;
    }
    
    /**
     * Clean up old records (should be called periodically)
     */
    public void cleanup() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        attempts.entrySet().removeIf(entry -> 
            entry.getValue().getLastAttempt().isBefore(cutoff)
        );
    }
}