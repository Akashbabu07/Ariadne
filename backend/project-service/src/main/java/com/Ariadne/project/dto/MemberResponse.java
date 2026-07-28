package com.Ariadne.project.dto;

import java.time.Instant;
import java.util.UUID;

public record MemberResponse(UUID id, UUID orgId, UUID userId, Instant joinedAt) {}
