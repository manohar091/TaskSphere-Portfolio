package com.tasksphere.domain.outbox;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    
    /**
     * Find unpublished events in order of creation, limited to avoid overwhelming the system
     */
    List<OutboxEvent> findTop100ByPublishedFalseOrderByCreatedAtAsc();
    
    /**
     * Count unpublished events for monitoring
     */
    long countByPublishedFalse();
}