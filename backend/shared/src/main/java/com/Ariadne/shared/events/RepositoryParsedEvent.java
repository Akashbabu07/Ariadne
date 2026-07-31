package com.Ariadne.shared.events;

import java.time.Instant;
import java.util.UUID;

public record RepositoryParsedEvent(
        UUID repositoryId,
        String gitUrl,
        int filesParsed,
        Instant occurredAt
) {}