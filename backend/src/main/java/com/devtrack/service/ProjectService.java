package com.devtrack.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import java.util.ArrayList;
import java.util.Comparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.devtrack.dto.ProjectRequest;
import com.devtrack.dto.ProjectResponse;
import com.devtrack.dto.ProjectStatusRequest;
import com.devtrack.entity.Project;
import com.devtrack.entity.User;
import com.devtrack.exception.ProjectNotFoundException;
import com.devtrack.repository.ProjectRepository;
import com.devtrack.repository.UserRepository;
import com.devtrack.repository.ProjectMemberRepository;

@Service
public class ProjectService {
    private final ProjectRepository projects;
    private final UserRepository users;
    private final ActivityService activity;
    private final ProjectAccessService access;
    private final ProjectMemberRepository members;
    public ProjectService(ProjectRepository projects, UserRepository users, ActivityService activity, ProjectAccessService access, ProjectMemberRepository members) { this.projects = projects; this.users = users; this.activity = activity; this.access = access; this.members = members; }

    private User owner(String email) { return users.findByEmail(email).orElseThrow(ProjectNotFoundException::new); }
    @Transactional
    public ProjectResponse create(String email, ProjectRequest request) {
        Project project = projects.save(new Project(request.name().trim(), request.description(), owner(email)));
        activity.record(project.getId(), email, "CREATED", "PROJECT", project.getId(), "Created project: " + project.getName());
        return ProjectResponse.from(project);
    }
    @Transactional(readOnly = true)
    public Page<ProjectResponse> list(String email, Pageable pageable) {
        Long userId = owner(email).getId();
        var visible = new ArrayList<>(projects.findAllByOwnerId(userId, Pageable.unpaged()).getContent());
        for (var member : members.findAllByUserId(userId)) {
            projects.findById(member.getProjectId()).ifPresent(project -> {
                if (visible.stream().noneMatch(existing -> existing.getId().equals(project.getId()))) visible.add(project);
            });
        }
        visible.sort(Comparator.comparing(Project::getCreatedAt).reversed());
        int start = (int) Math.min(pageable.getOffset(), visible.size());
        int end = Math.min(start + pageable.getPageSize(), visible.size());
        return new PageImpl<>(visible.subList(start, end).stream().map(ProjectResponse::from).toList(), pageable, visible.size());
    }
    @Transactional(readOnly = true)
    public ProjectResponse get(String email, Long id) { return ProjectResponse.from(access.requireView(email, id)); }
    @Transactional
    public ProjectResponse update(String email, Long id, ProjectRequest request) {
        Project project = access.requireOwner(email, id); project.setName(request.name().trim()); project.setDescription(request.description());
        activity.record(project.getId(), email, "UPDATED", "PROJECT", project.getId(), "Updated project: " + project.getName());
        return ProjectResponse.from(project);
    }
    @Transactional
    public ProjectResponse setStatus(String email, Long id, ProjectStatusRequest.Status status) {
        Project project = access.requireOwner(email, id);
        project.setStatus(status.name());
        activity.record(project.getId(), email, "UPDATED", "PROJECT", project.getId(),
                (status == ProjectStatusRequest.Status.ARCHIVED ? "Archived project: " : "Reactivated project: ") + project.getName());
        return ProjectResponse.from(project);
    }
    @Transactional
    public void delete(String email, Long id) { projects.delete(access.requireOwner(email, id)); }
}
