package com.tasksphere.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.tasksphere.domain.activity.ActivityLog;
import com.tasksphere.domain.activity.ActivityLogRepository;
import com.tasksphere.domain.user.User;
import com.tasksphere.domain.user.UserRepository;
import com.tasksphere.dto.activity.ActivityView;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {
    private final ActivityLogRepository repo;
    private final UserRepository userRepository;

    public List<ActivityView> list(String entityType, Long entityId) {
        return repo.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId)
                .stream()
                .map(a -> new ActivityView(a.getId(), a.getEntityType(), a.getEntityId(),
                        a.getActor().getName(), a.getAction(),
                        a.getFromValue(), a.getToValue(), a.getCreatedAt()))
                .toList();
    }

    /**
     * Log a security-sensitive event with full context
     * @param action The action performed (e.g., "LOGIN_SUCCESS", "PROJECT_DELETE")
     * @param entityType The type of entity (e.g., "USER", "PROJECT", "ISSUE")
     * @param entityId The ID of the entity affected
     * @param actorId The ID of the user performing the action
     * @param fromValue Previous value (for updates)
     * @param toValue New value (for updates)
     */
    public void logSecurityEvent(String action, String entityType, Long entityId, Long actorId, 
                                String fromValue, String toValue) {
        try {
            ActivityLog activity = new ActivityLog();
            activity.setAction(action);
            activity.setEntityType(entityType);
            activity.setEntityId(entityId);
            activity.setFromValue(fromValue);
            activity.setToValue(toValue);
            activity.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            
            if (actorId != null) {
                User actor = userRepository.findById(actorId)
                    .orElseThrow(() -> new RuntimeException("Actor not found: " + actorId));
                activity.setActor(actor);
            }
            
            repo.save(activity);
            
            // Also log to application logs for external monitoring
            log.info("Security Event: {} on {} [{}] by user [{}] - {} -> {}", 
                action, entityType, entityId, actorId, fromValue, toValue);
                
        } catch (Exception e) {
            log.error("Failed to log security event: {}", e.getMessage(), e);
        }
    }

    /**
     * Log authentication events
     * @param action LOGIN_SUCCESS, LOGIN_FAILURE, LOGOUT, TOKEN_REFRESH
     * @param email User email
     * @param userId User ID (null for failed logins)
     * @param ipAddress Client IP address
     */
    public void logAuthEvent(String action, String email, Long userId, String ipAddress) {
        try {
            ActivityLog activity = new ActivityLog();
            activity.setAction(action);
            activity.setEntityType("AUTH");
            activity.setEntityId(userId);
            activity.setFromValue(email);
            activity.setToValue(ipAddress);
            activity.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            
            if (userId != null) {
                User actor = userRepository.findById(userId).orElse(null);
                activity.setActor(actor);
            }
            
            repo.save(activity);
            
            // Enhanced logging for security monitoring
            if (action.contains("FAILURE")) {
                log.warn("Authentication failure: {} for {} from IP {}", action, email, ipAddress);
            } else {
                log.info("Authentication success: {} for user {} from IP {}", action, email, ipAddress);
            }
            
        } catch (Exception e) {
            log.error("Failed to log auth event: {}", e.getMessage(), e);
        }
    }

    /**
     * Log data access events
     * @param entityType Type of data accessed
     * @param entityId ID of the entity
     * @param action VIEW, EXPORT, DOWNLOAD
     */
    public void logDataAccess(String entityType, Long entityId, String action) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            User user = (User) auth.getPrincipal();
            logSecurityEvent(action, entityType, entityId, user.getId(), null, null);
        }
    }

    /**
     * Log administrative actions
     * @param action The admin action performed
     * @param targetUserId The user being affected by admin action
     * @param details Additional details about the action
     */
    public void logAdminAction(String action, Long targetUserId, String details) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            User admin = (User) auth.getPrincipal();
            logSecurityEvent(action, "ADMIN", targetUserId, admin.getId(), null, details);
        }
    }

    /**
     * Get security events for audit purposes
     * @param hours Number of hours to look back
     * @return List of recent security events
     */
    public List<ActivityView> getSecurityEvents(int hours) {
        return repo.findAll()
                .stream()
                .filter(a -> isSecurityRelevant(a.getAction()))
                .filter(a -> a.getCreatedAt().toLocalDateTime().isAfter(LocalDateTime.now().minusHours(hours)))
                .map(a -> new ActivityView(a.getId(), a.getEntityType(), a.getEntityId(),
                        a.getActor() != null ? a.getActor().getName() : "System", 
                        a.getAction(), a.getFromValue(), a.getToValue(), a.getCreatedAt()))
                .toList();
    }

    /**
     * Check if an action is security-relevant
     */
    private boolean isSecurityRelevant(String action) {
        return action.contains("LOGIN") || action.contains("AUTH") || 
               action.contains("DELETE") || action.contains("ADMIN") ||
               action.contains("ROLE") || action.contains("PERMISSION");
    }
}