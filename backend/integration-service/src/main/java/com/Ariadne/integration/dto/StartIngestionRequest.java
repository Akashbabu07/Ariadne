package com.Ariadne.integration.dto;

import java.util.UUID;

public record StartIngestionRequest(UUID repositoryId, String gitUrl) {}