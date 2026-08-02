package com.Ariadne.shared.events;

import java.time.Instant;
import java.util.UUID;

public record AnalysisCompletedEvent(
        UUID repositoryId,
        UUID analysisReportId,
        Instant occurredAt
) {}