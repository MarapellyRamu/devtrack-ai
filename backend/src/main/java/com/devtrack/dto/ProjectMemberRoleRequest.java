package com.devtrack.dto;

import com.devtrack.entity.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record ProjectMemberRoleRequest(@NotNull ProjectRole role) {}
