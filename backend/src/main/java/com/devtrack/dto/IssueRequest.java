package com.devtrack.dto;

import java.time.LocalDate;

import com.devtrack.entity.IssuePriority;
import com.devtrack.entity.IssueStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IssueRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 10000) String description,
        @NotNull IssuePriority priority,
        @NotNull IssueStatus status,
        LocalDate dueDate) {}
