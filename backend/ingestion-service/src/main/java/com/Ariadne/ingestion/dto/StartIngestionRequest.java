package com.Ariadne.ingestion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StartIngestionRequest(
        @NotNull UUID repositoryId,
        @NotBlank String gitUrl
) {}