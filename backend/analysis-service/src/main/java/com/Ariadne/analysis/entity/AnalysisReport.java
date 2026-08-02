package com.Ariadne.analysis.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "analysis_reports", schema = "analysis")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AnalysisReport {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "repository_id", nullable = false)
    private UUID repositoryId;

    @Column(name = "file_count", nullable = false)
    private int fileCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() { createdAt = Instant.now(); }
}