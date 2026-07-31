package com.Ariadne.search.service;

import com.Ariadne.search.client.EmbeddingClient;
import com.Ariadne.search.dto.SearchResultResponse;
import com.Ariadne.search.repository.SearchDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchDocumentRepository documentRepository;
    private final EmbeddingClient embeddingClient;

    public List<SearchResultResponse> keywordSearch(String query) {
        return documentRepository.searchByKeyword(query).stream()
                .map(d -> new SearchResultResponse(d.getRepositoryId(), d.getGitUrl(), d.getIndexedAt()))
                .toList();
    }


    public List<SearchResultResponse> semanticSearch(String query) {
        List<Float> queryVector = embeddingClient.embed(query);
        String vectorLiteral = "[" + queryVector.stream().map(String::valueOf).collect(Collectors.joining(",")) + "]";

        return documentRepository.searchByEmbedding(vectorLiteral).stream()
                .map(d -> new SearchResultResponse(d.getRepositoryId(), d.getGitUrl(), d.getIndexedAt()))
                .toList();
    }
}
