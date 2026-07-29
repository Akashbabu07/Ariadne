package com.Ariadne.aigateway.dto;

import java.util.List;

public record ParseResultResponse(String repositoryId, int filesParsed, List<String> filePaths) {}
