package com.devtrack.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.devtrack.dto.IssueSuggestionsResponse;
import com.devtrack.dto.ProjectResponse;

@Service
public class AiSuggestionService {
    private final RestClient client;

    public AiSuggestionService(RestClient.Builder builder,
            @Value("${devtrack.ai.url:http://localhost:8000}") String baseUrl) {
        this.client = builder.baseUrl(baseUrl).build();
    }

    public IssueSuggestionsResponse suggest(ProjectResponse project, String context, Integer count, String bearerToken) {
        return client.post()
                .uri("/api/ai/suggest-issues")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AiRequest(project.name(), project.description(), context == null ? "" : context, count == null ? 5 : count))
                .retrieve()
                .body(IssueSuggestionsResponse.class);
    }

    private record AiRequest(String project_name, String project_description, String context, int count) {
    }
}
