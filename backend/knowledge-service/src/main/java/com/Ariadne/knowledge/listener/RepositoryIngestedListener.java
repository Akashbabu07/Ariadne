package com.Ariadne.knowledge.listener;

import com.Ariadne.knowledge.entity.KnowledgeRecord;
import com.Ariadne.knowledge.repository.KnowledgeRecordRepository;
import com.Ariadne.shared.events.RepositoryIngestedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RepositoryIngestedListener {

    private static final Logger log = LoggerFactory.getLogger(RepositoryIngestedListener.class);

    private final KnowledgeRecordRepository recordRepository;

    @KafkaListener(topics = "repository.ingested", groupId = "knowledge-service")
    @Transactional
    public void onRepositoryIngested(RepositoryIngestedEvent event) {

        if (recordRepository.findByIngestionJobId(event.jobId()).isPresent()) {
            log.info("Duplicate event for jobId={}, skipping", event.jobId());
            return;
        }

        KnowledgeRecord record = KnowledgeRecord.builder()
                .repositoryId(event.repositoryId())
                .ingestionJobId(event.jobId())
                .gitUrl(event.gitUrl())
                .ingestionStatus(event.status())
                .build();
        recordRepository.save(record);

        log.info("Stored knowledge record for repositoryId={}", event.repositoryId());
    }
}