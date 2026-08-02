package com.Ariadne.auth.dto;

import jakarta.validation.constraints.NotNull;

public record RefreshRequest(@NotNull String refreshToken) {}