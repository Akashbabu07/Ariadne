package com.Ariadne.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateProjectRequest(
        @NotNull UUID orgId,
        @NotBlank String name,
        @Size(max = 1000) String description
) {}
