package com.Ariadne.project.dto;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(UUID id, UUID orgId, String name, String description, Instant createdAt) {}
