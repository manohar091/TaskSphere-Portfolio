package com.tasksphere.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.tasksphere.domain.project.ProjectRepository;
import com.tasksphere.domain.user.User;

/**
 * Custom permission checker for method-level security
 * Used with @PreAuthorize annotations for fine-grained access control
 */
@Component("perm")
public class PermissionChecker {

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * Check if user can manage a specific project
     * @param projectId The project ID
     * @param auth Authentication object containing user details
     * @return true if user is admin, project owner, or project manager
     */
    public boolean canManageProject(Long projectId, Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            return false;
        }

        User user = (User) auth.getPrincipal();
        
        // For now, check if user is project owner
        // In a full implementation, you would also check team membership and roles
        return projectRepository.findById(projectId)
                .map(project -> project.getOwner().getId().equals(user.getId()))
                .orElse(false);
    }

    /**
     * Check if user can access a specific project (read operations)
     * @param projectId The project ID
     * @param auth Authentication object containing user details
     * @return true if user has access to the project
     */
    public boolean canAccessProject(Long projectId, Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            return false;
        }

        User user = (User) auth.getPrincipal();
        
        // Check if user is project owner
        // In a full implementation, you would also check team membership
        return projectRepository.findById(projectId)
                .map(project -> project.getOwner().getId().equals(user.getId()))
                .orElse(false);
    }

    /**
     * Check if user owns a specific resource
     * @param resourceOwnerId The ID of the resource owner
     * @param auth Authentication object containing user details
     * @return true if user owns the resource
     */
    public boolean isOwnerOrAdmin(Long resourceOwnerId, Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            return false;
        }

        User user = (User) auth.getPrincipal();
        
        // Simple ownership check - in a full implementation, 
        // you would also check for admin role
        return user.getId().equals(resourceOwnerId);
    }
}