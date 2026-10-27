package com.devtrack.dto;

import jakarta.validation.constraints.NotNull;

public record ProjectStatusRequest(@NotNull Status status) {
    public enum Status { ACTIVE, ARCHIVED }
}
