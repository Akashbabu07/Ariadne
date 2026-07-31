package com.Ariadne.project.controller;

import com.Ariadne.project.dto.*;
import com.Ariadne.project.service.ProjectService;
import com.Ariadne.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping("/organizations")
    public ResponseEntity<ApiResponse<OrganizationResponse>> createOrg(@Valid @RequestBody CreateOrganizationRequest request) {
        if (!com.Ariadne.project.security.RequestContext.currentRoles().contains("ADMIN")) {
            throw new com.Ariadne.shared.exception.UnauthorizedException("Only ADMIN can create organizations");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(projectService.createOrganization(request)));
    }

    @GetMapping("/organizations/{orgId}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getOrg(@PathVariable UUID orgId) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getOrganization(orgId)));
    }

    @PostMapping("/projects")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(@Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(projectService.createProject(request)));
    }

    @GetMapping("/organizations/{orgId}/projects")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> listProjects(@PathVariable UUID orgId) {
        return ResponseEntity.ok(ApiResponse.success(projectService.listProjectsByOrg(orgId)));
    }

    @PostMapping("/repositories")
    public ResponseEntity<ApiResponse<RepositoryResponse>> registerRepo(@Valid @RequestBody RegisterRepositoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(projectService.registerRepository(request)));
    }

    @GetMapping("/projects/{projectId}/repositories")
    public ResponseEntity<ApiResponse<List<RepositoryResponse>>> listRepos(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.success(projectService.listRepositoriesByProject(projectId)));
    }

    @PostMapping("/members")
    public ResponseEntity<ApiResponse<MemberResponse>> addMember(@Valid @RequestBody AddMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(projectService.addMember(request)));
    }

    @GetMapping("/organizations/{orgId}/members")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> listMembers(@PathVariable UUID orgId) {
        return ResponseEntity.ok(ApiResponse.success(projectService.listMembersByOrg(orgId)));
    }

    @GetMapping("/repositories/stale")
    public ResponseEntity<ApiResponse<List<RepositoryResponse>>> getStaleRepositories(
            @RequestParam(defaultValue = "60") long minutes) {
        return ResponseEntity.ok(ApiResponse.success(projectService.listStaleRepositories(Duration.ofMinutes(minutes))));
    }

    @PatchMapping("/repositories/{repositoryId}/sync-status")
    public ResponseEntity<ApiResponse<RepositoryResponse>> markSynced(
            @PathVariable UUID repositoryId, @RequestParam com.Ariadne.project.entity.SyncStatus status) {
        return ResponseEntity.ok(ApiResponse.success(projectService.markRepositorySynced(repositoryId, status)));
    }
}
