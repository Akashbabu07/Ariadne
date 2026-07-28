package com.Ariadne.shared.events;

import java.time.Instant;
import java.util.UUID;

public record RepositoryIngestedEvent(
        UUID jobId,
        UUID repositoryId,
        String gitUrl,
        String status,
        Instant occurredAt
) {}