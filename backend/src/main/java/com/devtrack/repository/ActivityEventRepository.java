package com.devtrack.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.devtrack.entity.ActivityEvent;

public interface ActivityEventRepository extends JpaRepository<ActivityEvent, Long> {
    Page<ActivityEvent> findAllByProjectIdOrderByCreatedAtDesc(Long projectId, Pageable pageable);
}
