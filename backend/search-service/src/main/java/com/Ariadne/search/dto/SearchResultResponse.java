package com.Ariadne.search.dto;

import java.time.Instant;
import java.util.UUID;

public record SearchResultResponse(UUID repositoryId, String gitUrl, String filePath, String snippet, Instant indexedAt) {}