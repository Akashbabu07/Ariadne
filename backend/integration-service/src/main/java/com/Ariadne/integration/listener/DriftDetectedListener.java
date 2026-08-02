package com.Ariadne.integration.listener;

import com.Ariadne.integration.client.JiraClient;
import com.Ariadne.integration.client.SlackNotifier;
import com.Ariadne.shared.events.DriftDetectedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DriftDetectedListener {

    private static final Logger log = LoggerFactory.getLogger(DriftDetectedListener.class);

    private final SlackNotifier slackNotifier;
    private final JiraClient jiraClient;

    @KafkaListener(topics = "repository.drift-detected", groupId = "integration-service")
    public void onDriftDetected(DriftDetectedEvent event) {
        String summary = String.format("⚠️ Architecture drift detected — %d file(s) affected", event.driftedFileCount());
        log.info(summary);

        slackNotifier.send(summary + "\n" + event.explanation());
        jiraClient.createIssue(
                "Architecture drift: repository " + event.repositoryId(),
                event.explanation()
        );
    }
}