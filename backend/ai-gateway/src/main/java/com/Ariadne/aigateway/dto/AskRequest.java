package com.Ariadne.aigateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record AskRequest(
        UUID repositoryId,
        @Pattern(regexp = "qa|impact_analysis|drift_explanation") String mode,
        @NotBlank String query,
        String filePath
) {}