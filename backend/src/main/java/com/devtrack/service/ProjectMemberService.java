package com.devtrack.service;

import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.devtrack.dto.ProjectMemberRequest;
import com.devtrack.dto.ProjectMemberResponse;
import com.devtrack.entity.ProjectMember;
import com.devtrack.entity.ProjectRole;
import com.devtrack.exception.ProjectNotFoundException;
import com.devtrack.repository.ProjectMemberRepository;
import com.devtrack.repository.UserRepository;

@Service
public class ProjectMemberService {
    private final ProjectAccessService access;
    private final ProjectMemberRepository members;
    private final UserRepository users;
    private final ActivityService activity;
    public ProjectMemberService(ProjectAccessService access, ProjectMemberRepository members, UserRepository users, ActivityService activity) {
        this.access = access; this.members = members; this.users = users; this.activity = activity;
    }
    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> list(String actor, Long projectId) {
        access.requireView(actor, projectId);
        return members.findAllByProjectId(projectId).stream()
                .map(member -> users.findById(member.getUserId()).map(user -> ProjectMemberResponse.from(member, user)).orElse(null))
                .filter(java.util.Objects::nonNull).toList();
    }
    @Transactional
    public ProjectMemberResponse add(String actor, Long projectId, ProjectMemberRequest request) {
        access.requireOwner(actor, projectId);
        var user = users.findByEmail(request.email().trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new IllegalArgumentException("That email is not registered yet."));
        access.requireOwner(actor, projectId);
        if (members.existsByProjectIdAndUserId(projectId, user.getId())) throw new IllegalArgumentException("This user is already a project member.");
        ProjectMember member = members.save(new ProjectMember(projectId, user.getId(), request.role()));
        activity.record(projectId, actor, "ADDED", "MEMBER", user.getId(), "Added project member: " + user.getEmail());
        return ProjectMemberResponse.from(member, user);
    }
    @Transactional
    public ProjectMemberResponse updateRole(String actor, Long projectId, Long userId, ProjectRole role) {
        access.requireOwner(actor, projectId);
        ProjectMember member = members.findByProjectIdAndUserId(projectId, userId).orElseThrow(ProjectNotFoundException::new);
        member.setRole(role);
        var user = users.findById(userId).orElseThrow(ProjectNotFoundException::new);
        activity.record(projectId, actor, "UPDATED", "MEMBER", userId, "Changed " + user.getEmail() + " role to " + role.name());
        return ProjectMemberResponse.from(member, user);
    }
    @Transactional
    public void remove(String actor, Long projectId, Long userId) {
        access.requireOwner(actor, projectId);
        if (!members.existsByProjectIdAndUserId(projectId, userId)) throw new ProjectNotFoundException();
        members.deleteByProjectIdAndUserId(projectId, userId);
        String removedEmail = users.findById(userId).map(user -> user.getEmail()).orElse("a member");
        activity.record(projectId, actor, "REMOVED", "MEMBER", userId, "Removed project member: " + removedEmail);
    }
}
