package com.devtrack.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devtrack.entity.ActivityEvent;
import com.devtrack.service.ActivityService;
import com.devtrack.service.ProjectService;

@RestController
@RequestMapping("/api/projects/{projectId}/activity")
public class ActivityController {
    private final ProjectService projects;
    private final ActivityService activity;

    public ActivityController(ProjectService projects, ActivityService activity) {
        this.projects = projects;
        this.activity = activity;
    }

    @GetMapping
    public Page<ActivityEvent> list(@PathVariable Long projectId,
            @PageableDefault(size = 30, sort = "createdAt") Pageable pageable, Authentication auth) {
        projects.get(auth.getName(), projectId);
        return activity.list(projectId, pageable);
    }
}
