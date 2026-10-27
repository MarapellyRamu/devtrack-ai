package com.devtrack.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devtrack.dto.IssueRequest;
import com.devtrack.dto.IssueResponse;
import com.devtrack.entity.Issue;
import com.devtrack.entity.IssuePriority;
import com.devtrack.entity.IssueStatus;
import com.devtrack.entity.Project;
import com.devtrack.exception.IssueNotFoundException;
import com.devtrack.exception.ProjectNotFoundException;
import com.devtrack.repository.IssueRepository;

@Service
public class IssueService {
    private final IssueRepository issues;
    private final ProjectAccessService access;
    private final ActivityService activity;

    public IssueService(IssueRepository issues, ProjectAccessService access, ActivityService activity) {
        this.issues = issues;
        this.access = access;
        this.activity = activity;
    }

    private Project ownedProject(Long projectId, String email) {
        return access.requireView(email, projectId);
    }

    private Issue issueInProject(Long issueId, Long projectId) {
        return issues.findByIdAndProjectId(issueId, projectId).orElseThrow(IssueNotFoundException::new);
    }

    @Transactional
    public IssueResponse create(String email, Long projectId, IssueRequest request) {
        Project project = access.requireEdit(email, projectId);
        Issue issue = issues.save(new Issue(request.title().trim(), request.description(), request.priority(),
                request.status(), request.dueDate(), project));
        activity.record(project.getId(), email, "CREATED", "ISSUE", issue.getId(), "Created issue: " + issue.getTitle());
        return IssueResponse.from(issue);
    }

    @Transactional(readOnly = true)
    public Page<IssueResponse> list(String email, Long projectId, Pageable pageable,
            IssueStatus status, IssuePriority priority, String search) {
        Project project = ownedProject(projectId, email);
        String query = search == null || search.isBlank() ? null : search.trim();
        return issues.searchProjectIssues(project.getId(), status, priority, query, pageable).map(IssueResponse::from);
    }

    @Transactional(readOnly = true)
    public IssueResponse get(String email, Long projectId, Long issueId) {
        Project project = ownedProject(projectId, email);
        return IssueResponse.from(issueInProject(issueId, project.getId()));
    }

    @Transactional
    public IssueResponse update(String email, Long projectId, Long issueId, IssueRequest request) {
        Project project = access.requireEdit(email, projectId);
        Issue issue = issueInProject(issueId, project.getId());
        issue.setTitle(request.title().trim());
        issue.setDescription(request.description());
        issue.setPriority(request.priority());
        issue.setStatus(request.status());
        issue.setDueDate(request.dueDate());
        activity.record(project.getId(), email, "UPDATED", "ISSUE", issue.getId(), "Updated issue: " + issue.getTitle());
        return IssueResponse.from(issue);
    }

    @Transactional
    public void delete(String email, Long projectId, Long issueId) {
        Project project = access.requireEdit(email, projectId);
        Issue issue = issueInProject(issueId, project.getId());
        activity.record(project.getId(), email, "DELETED", "ISSUE", issue.getId(), "Deleted issue: " + issue.getTitle());
        issues.delete(issue);
    }
}
