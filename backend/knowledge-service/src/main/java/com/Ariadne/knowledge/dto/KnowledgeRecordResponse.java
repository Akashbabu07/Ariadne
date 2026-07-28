package com.Ariadne.knowledge.dto;

import java.time.Instant;
import java.util.UUID;

public record KnowledgeRecordResponse(
        UUID id, UUID repositoryId, UUID ingestionJobId,
        String gitUrl, String ingestionStatus, Instant receivedAt
) {}