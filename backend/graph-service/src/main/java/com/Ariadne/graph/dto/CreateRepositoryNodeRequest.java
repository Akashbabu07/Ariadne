package com.Ariadne.graph.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateRepositoryNodeRequest(
        @NotNull UUID repositoryId,
        @NotBlank String gitUrl
) {}