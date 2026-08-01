package com.Ariadne.integration.controller;

import com.Ariadne.integration.client.GitHubSignatureVerifier;
import com.Ariadne.integration.service.GitHubWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/integrations/github")
@RequiredArgsConstructor
public class GitHubWebhookController {

    private final GitHubSignatureVerifier signatureVerifier;
    private final GitHubWebhookService webhookService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader("X-Hub-Signature-256") String signature,
            @RequestHeader("X-GitHub-Event") String eventType,
            @RequestBody String rawBody) {

        if (!signatureVerifier.isValid(rawBody, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if ("push".equals(eventType)) {
            webhookService.handlePush(rawBody);
        }


        return ResponseEntity.ok().build();
    }
}