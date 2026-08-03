package com.Ariadne.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "atlas.jwt")
public record JwtProperties(
        String secret,
        long expirationMs,
        long refreshExpirationMs
) {}