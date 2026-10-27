package com.devtrack.dto;

import java.time.Instant;
import com.devtrack.entity.ProjectMember;
import com.devtrack.entity.ProjectRole;
import com.devtrack.entity.User;

public record ProjectMemberResponse(Long id, Long userId, String name, String email, ProjectRole role, Instant createdAt) {
    public static ProjectMemberResponse from(ProjectMember member, User user) {
        return new ProjectMemberResponse(member.getId(), user.getId(), user.getName(), user.getEmail(), member.getRole(), member.getCreatedAt());
    }
}
