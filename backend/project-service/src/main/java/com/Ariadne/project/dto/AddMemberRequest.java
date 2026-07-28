package com.Ariadne.project.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddMemberRequest(@NotNull UUID orgId, @NotNull UUID userId) {}
