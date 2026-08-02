package com.Ariadne.graph.dto;

import java.util.List;

public record IngestParsedFilesRequest(List<ParsedFileEntry> files) {

    public record ParsedFileEntry(
            String path,
            String language,
            List<String> imports
    ) {}
}
