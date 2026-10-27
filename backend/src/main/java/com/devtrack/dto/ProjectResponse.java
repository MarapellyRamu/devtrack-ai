package com.devtrack.dto;

import java.time.Instant;
import com.devtrack.entity.Project;

public record ProjectResponse(Long id, String name, String description, String status, Instant createdAt, Instant updatedAt, String ownerEmail) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(project.getId(), project.getName(), project.getDescription(), project.getStatus(), project.getCreatedAt(), project.getUpdatedAt(), project.getOwner().getEmail());
    }
}
