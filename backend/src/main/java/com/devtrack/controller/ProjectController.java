package com.devtrack.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.devtrack.dto.ProjectRequest;
import com.devtrack.dto.ProjectResponse;
import com.devtrack.dto.ProjectStatusRequest;
import com.devtrack.service.ProjectService;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService service;
    public ProjectController(ProjectService service) { this.service = service; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody ProjectRequest request, Authentication auth) { return service.create(auth.getName(), request); }
    @GetMapping
    public Page<ProjectResponse> list(@PageableDefault(size = 20, sort = "createdAt") Pageable pageable, Authentication auth) { return service.list(auth.getName(), pageable); }
    @GetMapping("/{id}")
    public ProjectResponse get(@PathVariable Long id, Authentication auth) { return service.get(auth.getName(), id); }
    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable Long id, @Valid @RequestBody ProjectRequest request, Authentication auth) { return service.update(auth.getName(), id, request); }
    @PatchMapping("/{id}/status")
    public ProjectResponse setStatus(@PathVariable Long id, @Valid @RequestBody ProjectStatusRequest request, Authentication auth) { return service.setStatus(auth.getName(), id, request.status()); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) { service.delete(auth.getName(), id); }
}
