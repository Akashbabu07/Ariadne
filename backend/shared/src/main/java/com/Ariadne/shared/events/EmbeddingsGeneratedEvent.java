package com.Ariadne.shared.events;

import java.time.Instant;
import java.util.UUID;

public record EmbeddingsGeneratedEvent(
        UUID repositoryId,
        int dimensions,
        Instant occurredAt
) {}