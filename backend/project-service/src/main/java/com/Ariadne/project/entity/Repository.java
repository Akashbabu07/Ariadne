package com.Ariadne.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "repositories", schema = "project")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Repository {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "git_url", nullable = false)
    private String gitUrl;

    @Column(name = "default_branch", nullable = false)
    @Builder.Default
    private String defaultBranch = "main";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SyncStatus syncStatus = SyncStatus.PENDING;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() { createdAt = Instant.now(); }
}
