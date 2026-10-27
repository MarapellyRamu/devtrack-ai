package com.devtrack.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.devtrack.dto.NotificationResponse;
import com.devtrack.service.ActivityService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final ActivityService activity;
    public NotificationController(ActivityService activity) { this.activity = activity; }
    @GetMapping
    public Page<NotificationResponse> list(@PageableDefault(size = 30, sort = "createdAt") Pageable pageable, Authentication auth) {
        return activity.notifications(auth.getName(), pageable);
    }
    @GetMapping("/unread-count")
    public UnreadCount unreadCount(Authentication auth) { return new UnreadCount(activity.unreadCount(auth.getName())); }
    @PatchMapping("/{id}/read") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@PathVariable Long id, Authentication auth) { activity.markRead(auth.getName(), id); }
    public record UnreadCount(long count) {}
}
