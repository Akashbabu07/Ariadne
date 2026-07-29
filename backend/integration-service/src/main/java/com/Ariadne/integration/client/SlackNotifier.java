package com.Ariadne.integration.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class SlackNotifier {

    private final RestClient restClient = RestClient.create();

    @Value("${integration.slack.webhook-url:}")
    private String webhookUrl;

    public void send(String message) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }
        restClient.post()
                .uri(webhookUrl)
                .body(Map.of("text", message))
                .retrieve()
                .toBodilessEntity();
    }
}