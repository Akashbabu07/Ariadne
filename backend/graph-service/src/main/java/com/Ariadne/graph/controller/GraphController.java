package com.Ariadne.graph.controller;

import com.Ariadne.graph.dto.*;
import com.Ariadne.graph.service.GraphService;
import com.Ariadne.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/graph")
@RequiredArgsConstructor
public class GraphController {

    private final GraphService graphService;

    @PostMapping("/repositories")
    public ResponseEntity<ApiResponse<RepositoryGraphResponse>> createRepo(@Valid @RequestBody CreateRepositoryNodeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(graphService.createRepositoryNode(request)));
    }

    @PostMapping("/repositories/{repositoryId}/files")
    public ResponseEntity<ApiResponse<RepositoryGraphResponse>> addFile(@PathVariable String repositoryId, @RequestBody AddFileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(graphService.addFile(repositoryId, request)));
    }

    @GetMapping("/repositories/{repositoryId}")
    public ResponseEntity<ApiResponse<RepositoryGraphResponse>> getGraph(@PathVariable String repositoryId) {
        return ResponseEntity.ok(ApiResponse.success(graphService.getRepositoryGraph(repositoryId)));
    }

    @PostMapping("/repositories/{repositoryId}/dependencies")
    public ResponseEntity<ApiResponse<Void>> addDependency(@PathVariable String repositoryId, @Valid @RequestBody AddDependencyRequest request) {
        graphService.addDependency(repositoryId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    @GetMapping("/repositories/{repositoryId}/dependencies")
    public ResponseEntity<ApiResponse<java.util.List<RepositoryGraphResponse.FileResponse>>> getDependencies(
            @PathVariable String repositoryId, @RequestParam String path) {
        return ResponseEntity.ok(ApiResponse.success(graphService.getDependencies(repositoryId, path)));
    }

    @GetMapping("/repositories/{repositoryId}/dependents")
    public ResponseEntity<ApiResponse<java.util.List<RepositoryGraphResponse.FileResponse>>> getDependents(
            @PathVariable String repositoryId, @RequestParam String path) {
        return ResponseEntity.ok(ApiResponse.success(graphService.getDependents(repositoryId, path)));
    }

    @PostMapping("/repositories/{repositoryId}/ingest-parsed")
    public ResponseEntity<ApiResponse<RepositoryGraphResponse>> ingestParsed(
            @PathVariable String repositoryId, @RequestBody IngestParsedFilesRequest request) {
        return ResponseEntity.ok(ApiResponse.success(graphService.ingestParsedFiles(repositoryId, request)));
    }
}