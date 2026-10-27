package com.devtrack.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record SuggestIssuesRequest(
        @Size(max = 10_000) String context,
        @Min(1) @Max(20) Integer count) {
}
