package com.Ariadne.search.service;

import com.Ariadne.search.dto.SearchResultResponse;
import com.Ariadne.search.repository.SearchDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchDocumentRepository documentRepository;

    public List<SearchResultResponse> keywordSearch(String query) {
        return documentRepository.searchByKeyword(query).stream()
                .map(d -> new SearchResultResponse(d.getRepositoryId(), d.getGitUrl(), d.getIndexedAt()))
                .toList();
    }

}