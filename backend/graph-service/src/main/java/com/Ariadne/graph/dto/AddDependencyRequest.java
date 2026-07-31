package com.Ariadne.graph.dto;

import jakarta.validation.constraints.NotBlank;

public record AddDependencyRequest(
        @NotBlank String fromPath,
        @NotBlank String toPath
) {}
