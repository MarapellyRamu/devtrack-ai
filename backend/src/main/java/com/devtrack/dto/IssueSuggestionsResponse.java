package com.devtrack.dto;

import java.util.List;

public record IssueSuggestionsResponse(List<Suggestion> issues, String model, String generatedBy) {
    public record Suggestion(String title, String description, String priority, String status) {
    }
}
