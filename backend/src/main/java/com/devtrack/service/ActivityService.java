package com.devtrack.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devtrack.entity.ActivityEvent;
import com.devtrack.entity.Notification;
import com.devtrack.repository.ActivityEventRepository;
import com.devtrack.repository.NotificationRepository;
import com.devtrack.repository.ProjectMemberRepository;
import com.devtrack.repository.ProjectRepository;
import com.devtrack.repository.UserRepository;
import com.devtrack.dto.NotificationResponse;
import com.devtrack.exception.NotificationNotFoundException;

@Service
public class ActivityService {
    private final ActivityEventRepository events;
    private final NotificationRepository notifications;
    private final ProjectRepository projects;
    private final ProjectMemberRepository members;
    private final UserRepository users;

    public ActivityService(ActivityEventRepository events, NotificationRepository notifications,
            ProjectRepository projects, ProjectMemberRepository members, UserRepository users) {
        this.events = events; this.notifications = notifications; this.projects = projects;
        this.members = members; this.users = users;
    }

    public void record(Long projectId, String actor, String action, String itemType, Long itemId, String summary) {
        ActivityEvent event = events.save(new ActivityEvent(projectId, actor, action, itemType, itemId, summary));
        Long actorId = users.findByEmail(actor).map(user -> user.getId()).orElse(null);
        var recipients = new java.util.HashSet<Long>();
        projects.findById(projectId).ifPresent(project -> recipients.add(project.getOwner().getId()));
        members.findAllByProjectId(projectId).forEach(member -> recipients.add(member.getUserId()));
        if (actorId != null) recipients.remove(actorId);
        notifications.saveAll(recipients.stream().map(userId -> new Notification(userId, event.getId())).toList());
    }

    @Transactional(readOnly = true)
    public Page<ActivityEvent> list(Long projectId, Pageable pageable) {
        return events.findAllByProjectIdOrderByCreatedAtDesc(projectId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> notifications(String email, Pageable pageable) {
        Long userId = users.findByEmail(email).orElseThrow().getId();
        return notifications.findAllByUserIdOrderByCreatedAtDesc(userId, pageable).map(notification -> {
            ActivityEvent event = events.findById(notification.getActivityEventId()).orElseThrow();
            return NotificationResponse.from(notification, event);
        });
    }

    @Transactional(readOnly = true)
    public long unreadCount(String email) {
        Long userId = users.findByEmail(email).orElseThrow().getId();
        return notifications.countByUserIdAndReadAtIsNull(userId);
    }

    @Transactional
    public void markRead(String email, Long notificationId) {
        Long userId = users.findByEmail(email).orElseThrow().getId();
        notifications.findByIdAndUserId(notificationId, userId).orElseThrow(NotificationNotFoundException::new).markRead();
    }
}
