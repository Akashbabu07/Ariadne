package com.Ariadne.analysis.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "drift_reports", schema = "analysis")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DriftReport {
    @Id @GeneratedValue private UUID id;

    @Column(name = "repository_id", nullable = false) private UUID repositoryId;
    @Column(name = "drifted_files", columnDefinition = "TEXT", nullable = false) private String driftedFilesJson;
    @Column(name = "explanation", columnDefinition = "TEXT") private String explanation;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    @PrePersist void onCreate() { createdAt = Instant.now(); }
}