package com.devtrack.dto;

import com.devtrack.entity.ProjectRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProjectMemberRequest(@NotBlank @Email @Size(max = 320) String email, @NotNull ProjectRole role) {}
