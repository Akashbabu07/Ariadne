package com.Ariadne.analysis.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "file_metric_snapshots", schema = "analysis")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FileMetricSnapshot {
    @Id @GeneratedValue private UUID id;

    @Column(name = "repository_id", nullable = false) private UUID repositoryId;
    @Column(name = "file_path", nullable = false, length = 1000) private String filePath;
    @Column(name = "fan_in", nullable = false) private int fanIn;
    @Column(name = "fan_out", nullable = false) private int fanOut;
    @Column(name = "captured_at", nullable = false, updatable = false) private Instant capturedAt;

    @PrePersist void onCreate() { capturedAt = Instant.now(); }
}