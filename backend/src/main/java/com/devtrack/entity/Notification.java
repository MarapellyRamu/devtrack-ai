package com.devtrack.entity;

import java.time.Instant;
import jakarta.persistence.*;

@Entity
@Table(name = "notifications", uniqueConstraints = @UniqueConstraint(name = "uq_notification_user_event", columnNames = {"user_id", "activity_event_id"}))
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(name = "activity_event_id", nullable = false) private Long activityEventId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "read_at") private Instant readAt;
    protected Notification() {}
    public Notification(Long userId, Long activityEventId) { this.userId = userId; this.activityEventId = activityEventId; }
    @PrePersist void setCreatedAt() { createdAt = Instant.now(); }
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getActivityEventId() { return activityEventId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getReadAt() { return readAt; }
    public void markRead() { if (readAt == null) readAt = Instant.now(); }
}
