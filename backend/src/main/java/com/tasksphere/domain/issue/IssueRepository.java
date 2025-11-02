package com.tasksphere.domain.issue;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {
    List<Issue> findByProjectIdAndStatus(Long projectId, String status);
    List<Issue> findByProjectIdAndAssigneeIdAndStatus(Long projectId, Long assigneeId, String status);
    List<Issue> findBySprintIdAndStatus(Long sprintId, String status);
    List<Issue> findByProjectId(Long projectId);
    
    @Query("SELECT i FROM Issue i WHERE i.project.id = :projectId AND (:status IS NULL OR i.status = :status)")
    List<Issue> findByProjectIdAndStatusOptional(@Param("projectId") Long projectId, @Param("status") String status);
}