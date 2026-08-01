package com.Ariadne.shared.events;

import java.time.Instant;
import java.util.UUID;

public record DriftDetectedEvent(UUID repositoryId, int driftedFileCount, String explanation, Instant occurredAt) {}