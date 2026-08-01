
package com.Ariadne.aigateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;

public record ReasoningResultDto(
        @JsonProperty("repository_id") UUID repositoryId,
        String mode,
        String answer,
        List<String> sources
) {}