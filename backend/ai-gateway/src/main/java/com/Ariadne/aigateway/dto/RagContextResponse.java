
package com.Ariadne.aigateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;

public record RagContextResponse(
        String query,
        @JsonProperty("repository_id") UUID repositoryId,
        List<ChunkDto> chunks,
        @JsonProperty("graph_context") GraphContextDto graphContext
) {
    public record ChunkDto(@JsonProperty("file_path") String filePath, String snippet, String source) {}
    public record GraphContextDto(@JsonProperty("file_path") String filePath, List<String> dependencies, List<String> dependents) {}
}