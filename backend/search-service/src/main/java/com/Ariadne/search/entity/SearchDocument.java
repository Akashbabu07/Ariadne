package com.Ariadne.search.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "search_documents", schema = "search")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SearchDocument {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "repository_id", nullable = false)
    private UUID repositoryId;

    @Column(name = "git_url", nullable = false)
    private String gitUrl;

    @Column(name = "indexed_at", nullable = false, updatable = false)
    private Instant indexedAt;

    @PrePersist
    void onCreate() { indexedAt = Instant.now(); }
}