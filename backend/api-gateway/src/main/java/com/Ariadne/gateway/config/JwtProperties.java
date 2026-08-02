package com.Ariadne.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "Ariadne.jwt")
public record JwtProperties(String secret) {}