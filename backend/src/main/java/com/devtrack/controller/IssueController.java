package com.devtrack.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.RequestParam;
import com.devtrack.entity.IssuePriority;
import com.devtrack.entity.IssueStatus;
import org.springframework.web.bind.annotation.RestController;

import com.devtrack.dto.IssueRequest;
import com.devtrack.dto.IssueResponse;
import com.devtrack.service.IssueService;

@RestController
@RequestMapping("/api/projects/{projectId}/issues")
public class IssueController {
    private final IssueService service;

    public IssueController(IssueService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IssueResponse create(@PathVariable Long projectId, @Valid @RequestBody IssueRequest request, Authentication auth) {
        return service.create(auth.getName(), projectId, request);
    }

    @GetMapping
    public Page<IssueResponse> list(@PathVariable Long projectId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) IssuePriority priority,
            @RequestParam(required = false) String search, Authentication auth) {
        return service.list(auth.getName(), projectId, pageable, status, priority, search);
    }

    @GetMapping("/{issueId}")
    public IssueResponse get(@PathVariable Long projectId, @PathVariable Long issueId, Authentication auth) {
        return service.get(auth.getName(), projectId, issueId);
    }

    @PutMapping("/{issueId}")
    public IssueResponse update(@PathVariable Long projectId, @PathVariable Long issueId,
            @Valid @RequestBody IssueRequest request, Authentication auth) {
        return service.update(auth.getName(), projectId, issueId, request);
    }

    @DeleteMapping("/{issueId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long projectId, @PathVariable Long issueId, Authentication auth) {
        service.delete(auth.getName(), projectId, issueId);
    }
}
