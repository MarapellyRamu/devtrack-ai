package com.devtrack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
class ProjectTeamAndNotificationTest {
    @Autowired MockMvc mvc;
    @Autowired ProjectRepository projects;
    @Autowired UserRepository users;
    @Autowired ObjectMapper mapper;

    @BeforeEach void clean() { projects.deleteAll(); users.deleteAll(); }

    private String register(String email) throws Exception {
        String body = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test User\",\"email\":\"" + email + "\",\"password\":\"password-123\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).path("accessToken").asText();
    }

    private long createProject(String token) throws Exception {
        String body = mvc.perform(post("/api/projects").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Shared project\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).path("id").asLong();
    }

    @Test void membersCanAccessSharedProjectsAndIssueWritesCreateUserScopedNotifications() throws Exception {
        String owner = register("team-owner@example.com");
        String member = register("team-member@example.com");
        long projectId = createProject(owner);

        mvc.perform(post("/api/projects/" + projectId + "/members").header("Authorization", "Bearer " + owner)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"team-member@example.com\",\"role\":\"MEMBER\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.role").value("MEMBER"));
        mvc.perform(get("/api/projects").header("Authorization", "Bearer " + member))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(projectId));
        mvc.perform(post("/api/projects/" + projectId + "/issues").header("Authorization", "Bearer " + member)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Shared task\",\"priority\":\"MEDIUM\",\"status\":\"OPEN\"}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/notifications/unread-count").header("Authorization", "Bearer " + member))
                .andExpect(status().isOk()).andExpect(jsonPath("$.count").value(1));

        String notification = mvc.perform(get("/api/notifications").header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].summary").value("Created issue: Shared task"))
                .andReturn().getResponse().getContentAsString();
        long notificationId = mapper.readTree(notification).path("content").get(0).path("id").asLong();
        mvc.perform(get("/api/notifications/unread-count").header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk()).andExpect(jsonPath("$.count").value(1));
        mvc.perform(patch("/api/notifications/" + notificationId + "/read").header("Authorization", "Bearer " + owner))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/notifications/unread-count").header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk()).andExpect(jsonPath("$.count").value(0));
        mvc.perform(patch("/api/notifications/" + notificationId + "/read").header("Authorization", "Bearer " + member))
                .andExpect(status().isNotFound());
    }

    @Test void viewersCanReadButCannotMutateAndOnlyOwnersCanManageMembership() throws Exception {
        String owner = register("viewer-owner@example.com");
        String viewer = register("viewer-user@example.com");
        long projectId = createProject(owner);
        mvc.perform(post("/api/projects/" + projectId + "/members").header("Authorization", "Bearer " + owner)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"viewer-user@example.com\",\"role\":\"VIEWER\"}"))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/projects/" + projectId).header("Authorization", "Bearer " + viewer)).andExpect(status().isOk());
        mvc.perform(post("/api/projects/" + projectId + "/issues").header("Authorization", "Bearer " + viewer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Forbidden task\",\"priority\":\"LOW\",\"status\":\"OPEN\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/projects/" + projectId + "/members/" + users.findByEmail("viewer-user@example.com").orElseThrow().getId())
                        .header("Authorization", "Bearer " + viewer))
                .andExpect(status().isNotFound());
    }
}
