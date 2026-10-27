package com.devtrack.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devtrack.dto.IssueSuggestionsResponse;
import com.devtrack.dto.SuggestIssuesRequest;
import com.devtrack.service.AiSuggestionService;
import com.devtrack.service.ProjectService;

@RestController
@RequestMapping("/api/projects/{projectId}/ai")
public class AiController {
    private final ProjectService projects;
    private final AiSuggestionService suggestions;

    public AiController(ProjectService projects, AiSuggestionService suggestions) {
        this.projects = projects;
        this.suggestions = suggestions;
    }

    @PostMapping("/suggest-issues")
    public IssueSuggestionsResponse suggest(@PathVariable Long projectId,
            @Valid @RequestBody SuggestIssuesRequest request, Authentication authentication,
            @RequestHeader("Authorization") String authorization) {
        var project = projects.get(authentication.getName(), projectId);
        return suggestions.suggest(project, request.context(), request.count(), authorization);
    }
}
