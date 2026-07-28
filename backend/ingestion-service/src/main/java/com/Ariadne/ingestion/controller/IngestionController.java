package com.Ariadne.ingestion.controller;

import com.Ariadne.ingestion.dto.*;
import com.Ariadne.ingestion.service.IngestionService;
import com.Ariadne.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ingestion")
@RequiredArgsConstructor
public class IngestionController {

    private final IngestionService ingestionService;

    @PostMapping
    public ResponseEntity<ApiResponse<IngestionJobResponse>> start(@Valid @RequestBody StartIngestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(ingestionService.startIngestion(request)));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ApiResponse<IngestionJobResponse>> getJob(@PathVariable UUID jobId) {
        return ResponseEntity.ok(ApiResponse.success(ingestionService.getJob(jobId)));
    }
}