package com.Ariadne.aigateway.controller;

import com.Ariadne.aigateway.dto.ParseResultResponse;
import com.Ariadne.aigateway.service.AiGatewayService;
import com.Ariadne.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiGatewayController {

    private final AiGatewayService aiGatewayService;

    @PostMapping("/parse")
    public ResponseEntity<ApiResponse<ParseResultResponse>> parse(
            @RequestParam String repositoryId, @RequestParam String gitUrl) {
        return ResponseEntity.ok(ApiResponse.success(aiGatewayService.triggerParse(repositoryId, gitUrl)));
    }
}
