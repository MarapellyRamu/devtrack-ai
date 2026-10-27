package com.devtrack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.devtrack.repository.IssueRepository;
import com.devtrack.repository.ProjectRepository;
import com.devtrack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IssueControllerTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired ProjectRepository projects;
    @Autowired IssueRepository issues;
    @Autowired ObjectMapper mapper;

    @BeforeEach void clean() { issues.deleteAll(); projects.deleteAll(); users.deleteAll(); }

    private String register(String email) throws Exception {
        String body = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test User\",\"email\":\"" + email + "\",\"password\":\"password-123\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).path("accessToken").asText();
    }

    private long createProject(String token, String name) throws Exception {
        String body = mvc.perform(post("/api/projects").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).path("id").asLong();
    }

    private String issueJson(String title, String priority, String state, String dueDate) {
        return "{\"title\":\"" + title + "\",\"description\":\"Initial description\",\"priority\":\"" + priority
                + "\",\"status\":\"" + state + "\",\"dueDate\":\"" + dueDate + "\"}";
    }

    private String createIssue(String token, long projectId, String title) throws Exception {
        return mvc.perform(post("/api/projects/" + projectId + "/issues").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(issueJson(title, "MEDIUM", "OPEN", "2026-12-31")))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    }

    @Test void createListRetrieveUpdateAndDeleteIssue() throws Exception {
        String token = register("issues-owner@example.com");
        long projectId = createProject(token, "Issue project");
        String created = createIssue(token, projectId, "First issue");
        long issueId = mapper.readTree(created).path("id").asLong();
        org.assertj.core.api.Assertions.assertThat(mapper.readTree(created).path("projectId").asLong()).isEqualTo(projectId);
        mvc.perform(get("/api/projects/" + projectId + "/issues?page=0&size=1").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("First issue"))
                .andExpect(jsonPath("$.page.size").value(1)).andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(1)).andExpect(jsonPath("$.page.totalPages").value(1));
        mvc.perform(get("/api/projects/" + projectId + "/issues/" + issueId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("First issue"));
        mvc.perform(put("/api/projects/" + projectId + "/issues/" + issueId).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Updated\",\"description\":\"Changed\",\"priority\":\"CRITICAL\",\"status\":\"IN_PROGRESS\",\"dueDate\":\"2027-01-15\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Updated"))
                .andExpect(jsonPath("$.description").value("Changed")).andExpect(jsonPath("$.priority").value("CRITICAL"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS")).andExpect(jsonPath("$.dueDate").value("2027-01-15"));
        mvc.perform(delete("/api/projects/" + projectId + "/issues/" + issueId).header("Authorization", "Bearer " + token)).andExpect(status().isNoContent());
        mvc.perform(get("/api/projects/" + projectId + "/issues/" + issueId).header("Authorization", "Bearer " + token)).andExpect(status().isNotFound());
    }

    @Test void invalidTitlesAndEnumValuesAreRejected() throws Exception {
        String token = register("issues-validation@example.com");
        long projectId = createProject(token, "Validation project");
        mvc.perform(post("/api/projects/" + projectId + "/issues").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(issueJson(" ", "LOW", "OPEN", "2026-12-31")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.title").exists());
        mvc.perform(post("/api/projects/" + projectId + "/issues").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(issueJson("Bad enum", "URGENT", "OPEN", "2026-12-31")))
                .andExpect(status().isBadRequest());
    }

    @Test void issueListFiltersByStatusPriorityAndSearchWhileKeepingPagination() throws Exception {
        String token = register("issues-filtering@example.com");
        long projectId = createProject(token, "Filtering project");
        mvc.perform(post("/api/projects/" + projectId + "/issues").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(issueJson("Searchable bug", "LOW", "OPEN", "2026-12-31")))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/projects/" + projectId + "/issues").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(issueJson("Another task", "HIGH", "IN_PROGRESS", "2026-12-31")))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/projects/" + projectId + "/issues?status=OPEN&priority=LOW&search=searchable&page=0&size=10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Searchable bug"));
        mvc.perform(get("/api/projects/" + projectId + "/issues?page=1&size=1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page.totalPages").value(2));
    }

    @Test void missingAndUnauthorizedResourcesReturnNotFoundAndUnauthenticatedReturns401() throws Exception {
        mvc.perform(get("/api/projects/1/issues")).andExpect(status().isUnauthorized());
        String token = register("issues-not-found@example.com");
        mvc.perform(get("/api/projects/9999/issues").header("Authorization", "Bearer " + token)).andExpect(status().isNotFound());
        long projectId = createProject(token, "Existing");
        mvc.perform(get("/api/projects/" + projectId + "/issues/9999").header("Authorization", "Bearer " + token)).andExpect(status().isNotFound());
    }

    @Test void projectOwnershipAndIssueProjectMembershipAreEnforced() throws Exception {
        String first = register("issues-first@example.com");
        long firstProject = createProject(first, "First project");
        String issue = createIssue(first, firstProject, "Private issue");
        long issueId = mapper.readTree(issue).path("id").asLong();
        long anotherProject = createProject(first, "Another project");
        String second = register("issues-second@example.com");
        mvc.perform(get("/api/projects/" + firstProject + "/issues").header("Authorization", "Bearer " + second)).andExpect(status().isNotFound());
        mvc.perform(get("/api/projects/" + firstProject + "/issues/" + issueId).header("Authorization", "Bearer " + second)).andExpect(status().isNotFound());
        mvc.perform(put("/api/projects/" + firstProject + "/issues/" + issueId).header("Authorization", "Bearer " + second)
                        .contentType(MediaType.APPLICATION_JSON).content(issueJson("Hijacked", "LOW", "OPEN", "2026-12-31")))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/projects/" + firstProject + "/issues/" + issueId).header("Authorization", "Bearer " + second)).andExpect(status().isNotFound());
        mvc.perform(get("/api/projects/" + anotherProject + "/issues/" + issueId).header("Authorization", "Bearer " + first)).andExpect(status().isNotFound());
    }
}
