package com.devtrack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.devtrack.repository.ProjectRepository;
import com.devtrack.repository.IssueRepository;
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

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class ProjectControllerTest {
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
    private String bearer(String token) { return "Bearer " + token; }
    private String json(String name) { return "{\"name\":\"" + name + "\",\"description\":\"sample\"}"; }

    @Test void createListPaginateUpdateAndDeleteOwnedProjects() throws Exception {
        String token = register("owner@example.com");
        String created = mvc.perform(post("/api/projects").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content(json("Alpha")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("Alpha")).andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(created).path("id").asLong();
        mvc.perform(post("/api/projects").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content(json("Beta"))).andExpect(status().isCreated());
        mvc.perform(get("/api/projects?page=0&size=1").header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").isNumber()).andExpect(jsonPath("$.page.size").value(1))
                .andExpect(jsonPath("$.page.number").value(0)).andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.page.totalPages").value(2));
        mvc.perform(get("/api/projects/" + id).header("Authorization", bearer(token))).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        mvc.perform(put("/api/projects/" + id).header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content(json("Updated"))).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Updated"));
        mvc.perform(delete("/api/projects/" + id).header("Authorization", bearer(token))).andExpect(status().isNoContent());
        mvc.perform(get("/api/projects/" + id).header("Authorization", bearer(token))).andExpect(status().isNotFound());
    }

    @Test void unauthorizedInvalidAndForeignProjectRequestsAreHandled() throws Exception {
        mvc.perform(get("/api/projects")).andExpect(status().isUnauthorized());
        String first = register("first@example.com");
        mvc.perform(post("/api/projects").header("Authorization", bearer(first)).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\" \"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.name").exists());
        String created = mvc.perform(post("/api/projects").header("Authorization", bearer(first)).contentType(MediaType.APPLICATION_JSON).content(json("Private")))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(created).path("id").asLong();
        String second = register("second@example.com");
        mvc.perform(get("/api/projects/" + id).header("Authorization", bearer(second))).andExpect(status().isNotFound());
        mvc.perform(put("/api/projects/" + id).header("Authorization", bearer(second)).contentType(MediaType.APPLICATION_JSON).content(json("Hijack"))).andExpect(status().isNotFound());
        mvc.perform(delete("/api/projects/" + id).header("Authorization", bearer(second))).andExpect(status().isNotFound());
    }
}
