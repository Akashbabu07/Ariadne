package com.Ariadne.aigateway.controller;

import com.Ariadne.aigateway.dto.AskRequest;
import com.Ariadne.aigateway.dto.AskResponse;
import com.Ariadne.aigateway.dto.ParseResultResponse;
import com.Ariadne.aigateway.service.AiGatewayService;
import com.Ariadne.shared.response.ApiResponse;
import jakarta.validation.Valid;
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

    @PostMapping("/ask")
    public ResponseEntity<ApiResponse<AskResponse>> ask(@Valid @RequestBody AskRequest request) {
        return ResponseEntity.ok(ApiResponse.success(aiGatewayService.ask(request)));
    }
}
