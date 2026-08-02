
package com.Ariadne.graph.dto;

public record FileMetricsResponse(String path, String language, long fanIn, long fanOut) {}