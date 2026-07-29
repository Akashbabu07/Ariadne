package com.Ariadne.analysis.controller;

import com.Ariadne.analysis.dto.AnalysisReportResponse;
import com.Ariadne.analysis.service.AnalysisService;
import com.Ariadne.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping("/repositories/{repositoryId}/run")
    public ResponseEntity<ApiResponse<AnalysisReportResponse>> run(@PathVariable UUID repositoryId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(analysisService.runBasicAnalysis(repositoryId)));
    }
}