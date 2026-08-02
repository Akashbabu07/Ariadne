package com.Ariadne.analysis.dto;

public record FileMetricsResponse(String path, String language, long fanIn, long fanOut) {}