package com.devtrack.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "activity_events")
public class ActivityEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "actor_email", nullable = false, length = 320)
    private String actorEmail;

    @Column(nullable = false, length = 40)
    private String action;

    @Column(name = "item_type", nullable = false, length = 20)
    private String itemType;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(nullable = false, length = 500)
    private String summary;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ActivityEvent() {}

    public ActivityEvent(Long projectId, String actorEmail, String action, String itemType, Long itemId, String summary) {
        this.projectId = projectId;
        this.actorEmail = actorEmail;
        this.action = action;
        this.itemType = itemType;
        this.itemId = itemId;
        this.summary = summary;
    }

    @PrePersist void setCreatedAt() { createdAt = Instant.now(); }
    public Long getId() { return id; }
    public Long getProjectId() { return projectId; }
    public String getActorEmail() { return actorEmail; }
    public String getAction() { return action; }
    public String getItemType() { return itemType; }
    public Long getItemId() { return itemId; }
    public String getSummary() { return summary; }
    public Instant getCreatedAt() { return createdAt; }
}
