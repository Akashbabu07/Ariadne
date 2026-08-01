package com.Ariadne.search.dto;

import java.util.List;

public record IngestParsedFilesRequest(List<ParsedFileDto> files) {
    public record ParsedFileDto(String path, String language, String content) {}
}