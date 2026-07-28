package com.Ariadne.knowledge.service;

import com.Ariadne.knowledge.dto.KnowledgeRecordResponse;
import com.Ariadne.knowledge.entity.KnowledgeRecord;
import com.Ariadne.knowledge.repository.KnowledgeRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeRecordRepository recordRepository;

    public List<KnowledgeRecordResponse> getByRepository(UUID repositoryId) {
        return recordRepository.findByRepositoryId(repositoryId).stream()
                .map(this::toResponse)
                .toList();
    }

    private KnowledgeRecordResponse toResponse(KnowledgeRecord r) {
        return new KnowledgeRecordResponse(
                r.getId(), r.getRepositoryId(), r.getIngestionJobId(),
                r.getGitUrl(), r.getIngestionStatus(), r.getReceivedAt()
        );
    }
}