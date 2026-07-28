package com.Ariadne.knowledge.repository;

import com.Ariadne.knowledge.entity.KnowledgeRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KnowledgeRecordRepository extends JpaRepository<KnowledgeRecord, UUID> {
    Optional<KnowledgeRecord> findByIngestionJobId(UUID ingestionJobId);
    List<KnowledgeRecord> findByRepositoryId(UUID repositoryId);
}