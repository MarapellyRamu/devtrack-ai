package com.devtrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectRequest(@NotBlank @Size(max = 120) String name, @Size(max = 10000) String description) {}
