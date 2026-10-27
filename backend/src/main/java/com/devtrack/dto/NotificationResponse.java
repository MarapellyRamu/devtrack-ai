package com.devtrack.dto;

import java.time.Instant;
import com.devtrack.entity.ActivityEvent;
import com.devtrack.entity.Notification;

public record NotificationResponse(Long id, Long projectId, String summary, String actorEmail,
        String action, String itemType, Long itemId, Instant createdAt, Instant readAt) {
    public static NotificationResponse from(Notification notification, ActivityEvent event) {
        return new NotificationResponse(notification.getId(), event.getProjectId(), event.getSummary(),
                event.getActorEmail(), event.getAction(), event.getItemType(), event.getItemId(),
                notification.getCreatedAt(), notification.getReadAt());
    }
}
