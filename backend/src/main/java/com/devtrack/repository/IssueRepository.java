package com.devtrack.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.devtrack.entity.Issue;
import com.devtrack.entity.IssuePriority;
import com.devtrack.entity.IssueStatus;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

public interface IssueRepository extends JpaRepository<Issue, Long> {
    Page<Issue> findAllByProjectId(Long projectId, Pageable pageable);
    @Query("""
            SELECT i FROM Issue i WHERE i.project.id = :projectId
            AND (:status IS NULL OR i.status = :status)
            AND (:priority IS NULL OR i.priority = :priority)
            AND (:search IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(COALESCE(i.description, '')) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Issue> searchProjectIssues(@Param("projectId") Long projectId,
            @Param("status") IssueStatus status, @Param("priority") IssuePriority priority,
            @Param("search") String search, Pageable pageable);
    Optional<Issue> findByIdAndProjectId(Long id, Long projectId);
}
