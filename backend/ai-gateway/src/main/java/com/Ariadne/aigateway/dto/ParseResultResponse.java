package com.Ariadne.aigateway.dto;

import java.util.List;

public record ParseResultResponse(String repositoryId, int filesParsed, List<ParsedFile> files) {

    public record ParsedFile(String path, String language, int lineCount, List<String> imports) {}
}
