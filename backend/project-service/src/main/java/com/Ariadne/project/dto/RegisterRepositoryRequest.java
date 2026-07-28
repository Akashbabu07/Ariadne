package com.Ariadne.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RegisterRepositoryRequest(
        @NotNull UUID projectId,
        @NotBlank String gitUrl,
        String defaultBranch
) {}
