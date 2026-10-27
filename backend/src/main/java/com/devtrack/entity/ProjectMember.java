package com.devtrack.entity;

import java.time.Instant;
import jakarta.persistence.*;

@Entity
@Table(name = "project_members", uniqueConstraints = @UniqueConstraint(name = "uq_project_member", columnNames = {"project_id", "user_id"}))
public class ProjectMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "project_id", nullable = false) private Long projectId;
    @Column(name = "user_id", nullable = false) private Long userId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ProjectRole role;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    protected ProjectMember() {}
    public ProjectMember(Long projectId, Long userId, ProjectRole role) { this.projectId = projectId; this.userId = userId; this.role = role; }
    @PrePersist void setCreatedAt() { createdAt = Instant.now(); }
    public Long getId() { return id; }
    public Long getProjectId() { return projectId; }
    public Long getUserId() { return userId; }
    public ProjectRole getRole() { return role; }
    public void setRole(ProjectRole role) { this.role = role; }
    public Instant getCreatedAt() { return createdAt; }
}
