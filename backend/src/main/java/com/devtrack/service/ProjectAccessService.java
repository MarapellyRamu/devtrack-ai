package com.devtrack.service;

import org.springframework.stereotype.Service;
import com.devtrack.entity.Project;
import com.devtrack.entity.ProjectRole;
import com.devtrack.exception.ProjectNotFoundException;
import com.devtrack.repository.ProjectMemberRepository;
import com.devtrack.repository.ProjectRepository;
import com.devtrack.repository.UserRepository;

@Service
public class ProjectAccessService {
    private final ProjectRepository projects;
    private final ProjectMemberRepository members;
    private final UserRepository users;
    public ProjectAccessService(ProjectRepository projects, ProjectMemberRepository members, UserRepository users) {
        this.projects = projects; this.members = members; this.users = users;
    }
    public Project requireView(String email, Long projectId) {
        var user = users.findByEmail(email).orElseThrow(ProjectNotFoundException::new);
        Project project = projects.findById(projectId).orElseThrow(ProjectNotFoundException::new);
        if (project.getOwner().getId().equals(user.getId()) || members.existsByProjectIdAndUserId(projectId, user.getId())) return project;
        throw new ProjectNotFoundException();
    }
    public Project requireEdit(String email, Long projectId) {
        Project project = requireView(email, projectId);
        Long userId = users.findByEmail(email).orElseThrow(ProjectNotFoundException::new).getId();
        if (project.getOwner().getId().equals(userId)) return project;
        var member = members.findByProjectIdAndUserId(projectId, userId).orElseThrow(ProjectNotFoundException::new);
        if (member.getRole() != ProjectRole.VIEWER) return project;
        throw new ProjectNotFoundException();
    }
    public Project requireOwner(String email, Long projectId) {
        Long userId = users.findByEmail(email).orElseThrow(ProjectNotFoundException::new).getId();
        return projects.findByIdAndOwnerId(projectId, userId).orElseThrow(ProjectNotFoundException::new);
    }
}
