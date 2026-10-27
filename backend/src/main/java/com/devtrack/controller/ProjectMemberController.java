package com.devtrack.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.devtrack.dto.ProjectMemberRequest;
import com.devtrack.dto.ProjectMemberResponse;
import com.devtrack.dto.ProjectMemberRoleRequest;
import com.devtrack.service.ProjectMemberService;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMemberController {
    private final ProjectMemberService members;
    public ProjectMemberController(ProjectMemberService members) { this.members = members; }
    @GetMapping public List<ProjectMemberResponse> list(@PathVariable Long projectId, Authentication auth) {
        return members.list(auth.getName(), projectId);
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ProjectMemberResponse add(@PathVariable Long projectId, @Valid @RequestBody ProjectMemberRequest request, Authentication auth) {
        return members.add(auth.getName(), projectId, request);
    }
    @PutMapping("/{userId}")
    public ProjectMemberResponse update(@PathVariable Long projectId, @PathVariable Long userId,
            @Valid @RequestBody ProjectMemberRoleRequest request, Authentication auth) {
        return members.updateRole(auth.getName(), projectId, userId, request.role());
    }
    @DeleteMapping("/{userId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long projectId, @PathVariable Long userId, Authentication auth) {
        members.remove(auth.getName(), projectId, userId);
    }
}
