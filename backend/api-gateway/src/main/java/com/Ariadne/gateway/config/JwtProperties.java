package com.Ariadne.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ariadne.jwt")
public record JwtProperties(String secret) {}