package com.devtrack.dto;

import java.time.Instant;
import java.time.LocalDate;

import com.devtrack.entity.Issue;
import com.devtrack.entity.IssuePriority;
import com.devtrack.entity.IssueStatus;

public record IssueResponse(Long id, Long projectId, String title, String description, IssuePriority priority,
        IssueStatus status, LocalDate dueDate, Instant createdAt, Instant updatedAt) {
    public static IssueResponse from(Issue issue) {
        return new IssueResponse(issue.getId(), issue.getProject().getId(), issue.getTitle(), issue.getDescription(),
                issue.getPriority(), issue.getStatus(), issue.getDueDate(), issue.getCreatedAt(), issue.getUpdatedAt());
    }
}
