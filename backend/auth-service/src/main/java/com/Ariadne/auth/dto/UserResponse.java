package com.Ariadne.auth.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        UUID orgId,
        String email,
        String status,
        List<String> roles,
        Instant createdAt
) {}