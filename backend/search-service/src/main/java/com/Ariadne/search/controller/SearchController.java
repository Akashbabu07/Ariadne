package com.Ariadne.search.controller;

import com.Ariadne.search.dto.IngestParsedFilesRequest;
import com.Ariadne.search.dto.SearchResultResponse;
import com.Ariadne.search.service.SearchService;
import com.Ariadne.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SearchResultResponse>>> search(@RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.success(searchService.keywordSearch(q)));
    }

    @GetMapping("/semantic")
    public ResponseEntity<ApiResponse<List<SearchResultResponse>>> semanticSearch(@RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.success(searchService.semanticSearch(q)));
    }

    @PostMapping("/repositories/{repositoryId}/ingest-parsed")
    public ResponseEntity<ApiResponse<Void>> ingestParsedFiles(
            @PathVariable UUID repositoryId,
            @RequestParam String gitUrl,
            @RequestBody IngestParsedFilesRequest request) {
        searchService.indexParsedFiles(repositoryId, gitUrl, request.files());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    @GetMapping("/hybrid")
    public ResponseEntity<ApiResponse<List<SearchResultResponse>>> hybridSearch(
            @RequestParam String q,
            @RequestParam(required = false) UUID repositoryId) {
        return ResponseEntity.ok(ApiResponse.success(searchService.hybridSearch(q, repositoryId)));
    }
}