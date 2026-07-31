package com.Ariadne.shared.events;

import java.time.Instant;
import java.util.UUID;

public record DependenciesExtractedEvent(
        UUID repositoryId,
        int dependencyEdgeCount,
        Instant occurredAt
) {}