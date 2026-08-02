package com.Ariadne.scheduler.dto;

public record IngestionJobResponse(
        String id, String repositoryId, String gitUrl, String status,
        String errorMessage, String createdAt, String updatedAt
) {}