package com.Ariadne.search.controller;

import com.Ariadne.search.dto.SearchResultResponse;
import com.Ariadne.search.service.SearchService;
import com.Ariadne.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SearchResultResponse>>> search(@RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.success(searchService.keywordSearch(q)));
    }
}