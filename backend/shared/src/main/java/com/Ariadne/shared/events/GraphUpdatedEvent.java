
package com.Ariadne.shared.events;

import java.time.Instant;
import java.util.UUID;

public record GraphUpdatedEvent(
        UUID repositoryId,
        int fileCount,
        Instant occurredAt
) {}