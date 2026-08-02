package com.Ariadne.analysis.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "impact_analysis_reports", schema = "analysis")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ImpactAnalysisReport {
    @Id @GeneratedValue private UUID id;

    @Column(name = "repository_id", nullable = false) private UUID repositoryId;
    @Column(name = "target_path", nullable = false, length = 1000) private String targetPath;
    @Column(name = "impacted_files", columnDefinition = "TEXT", nullable = false) private String impactedFilesJson;
    @Column(name = "explanation", columnDefinition = "TEXT") private String explanation;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    @PrePersist void onCreate() { createdAt = Instant.now(); }
}