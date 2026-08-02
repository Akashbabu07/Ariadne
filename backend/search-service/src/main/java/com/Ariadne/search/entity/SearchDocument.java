package com.Ariadne.search.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "search_documents", schema = "search")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SearchDocument {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "repository_id", nullable = false)
    private UUID repositoryId;

    @Column(name = "git_url", nullable = false, length = 500)
    private String gitUrl;

    @Column(name = "file_path", nullable = false, length = 1000)
    private String filePath;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "indexed_at", nullable = false)
    @Builder.Default
    private Instant indexedAt = Instant.now();
}