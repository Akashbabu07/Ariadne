package com.Ariadne.analysis.dto;

import java.util.List;

public record RepositoryGraphResponse(
        String repositoryId,
        String gitUrl,
        List<FileResponse> files
) {
    public record FileResponse(Long id, String path, String language) {}
}