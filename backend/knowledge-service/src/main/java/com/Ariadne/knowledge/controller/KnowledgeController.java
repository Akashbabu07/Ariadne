package com.Ariadne.knowledge.controller;

import com.Ariadne.knowledge.dto.KnowledgeRecordResponse;
import com.Ariadne.knowledge.service.KnowledgeService;
import com.Ariadne.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @GetMapping("/repositories/{repositoryId}")
    public ResponseEntity<ApiResponse<List<KnowledgeRecordResponse>>> getByRepository(@PathVariable UUID repositoryId) {
        return ResponseEntity.ok(ApiResponse.success(knowledgeService.getByRepository(repositoryId)));
    }
}