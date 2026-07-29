package com.Ariadne.integration.listener;

import com.Ariadne.integration.client.SlackNotifier;
import com.Ariadne.shared.events.RepositoryIngestedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RepositoryIngestedListener {

    private static final Logger log = LoggerFactory.getLogger(RepositoryIngestedListener.class);

    private final SlackNotifier slackNotifier;

    @KafkaListener(topics = "repository.ingested", groupId = "integration-service")
    public void onRepositoryIngested(RepositoryIngestedEvent event) {
        String message = String.format(
                "📦 Repository ingested: %s (status: %s)", event.gitUrl(), event.status()
        );
        log.info(message);
        slackNotifier.send(message);
    }
}