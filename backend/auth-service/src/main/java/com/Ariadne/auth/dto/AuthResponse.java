package com.Ariadne.auth.dto;
public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresInSeconds
) {}