package com.Ariadne.knowledge.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "knowledge_records", schema = "knowledge")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KnowledgeRecord {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "repository_id", nullable = false)
    private UUID repositoryId;

    @Column(name = "ingestion_job_id", nullable = false, unique = true)
    private UUID ingestionJobId;


    @Column(name = "git_url", nullable = false)
    private String gitUrl;

    @Column(name = "ingestion_status", nullable = false)
    private String ingestionStatus;

    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    @PrePersist
    void onCreate() { receivedAt = Instant.now(); }
}