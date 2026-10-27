package com.devtrack.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.devtrack.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findAllByOwnerId(Long ownerId, Pageable pageable);
    Optional<Project> findByIdAndOwnerId(Long id, Long ownerId);
}
