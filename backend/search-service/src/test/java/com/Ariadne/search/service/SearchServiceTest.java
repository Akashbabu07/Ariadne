package com.Ariadne.search.service;

import com.Ariadne.search.client.EmbeddingClient;
import com.Ariadne.search.dto.IngestParsedFilesRequest;
import com.Ariadne.search.entity.SearchDocument;
import com.Ariadne.search.repository.SearchDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SearchServiceTest {

    @Mock private SearchDocumentRepository documentRepository;
    @Mock private EmbeddingClient embeddingClient;
    @Mock private KafkaTemplate<String, Object> kafkaTemplate;

    private SearchService searchService;
    private final UUID repositoryId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        searchService = new SearchService(documentRepository, embeddingClient, kafkaTemplate);
    }

    @Test
    void indexParsedFiles_oneFileFailsEmbedding_othersStillIndexedAndEventStillFires() {
        var goodFile = new IngestParsedFilesRequest.ParsedFileDto("src/good.py", "python", "print('hi')");
        var badFile = new IngestParsedFilesRequest.ParsedFileDto("src/bad.py", "python", "broken");

        when(documentRepository.findByRepositoryIdAndFilePath(eq(repositoryId), anyString()))
                .thenReturn(Optional.empty());
        when(documentRepository.save(any(SearchDocument.class)))
                .thenAnswer(inv -> {
                    SearchDocument d = inv.getArgument(0);
                    d.setId(UUID.randomUUID());
                    return d;
                });
        when(embeddingClient.embed("print('hi')")).thenReturn(List.of(0.1f, 0.2f));
        when(embeddingClient.embed("broken")).thenThrow(new RuntimeException("embedding service down"));

        searchService.indexParsedFiles(repositoryId, "https://github.com/x/y.git", List.of(goodFile, badFile));


        verify(documentRepository, times(2)).save(any(SearchDocument.class));
        verify(documentRepository, times(1)).updateEmbedding(any(UUID.class), anyString());

        verify(kafkaTemplate, times(1)).send(eq("repository.embeddings-generated"), eq(repositoryId.toString()), any());
    }

    @Test
    void indexParsedFiles_allEmbeddingsFail_noEventPublished() {
        var file = new IngestParsedFilesRequest.ParsedFileDto("src/a.py", "python", "x");
        when(documentRepository.findByRepositoryIdAndFilePath(eq(repositoryId), anyString())).thenReturn(Optional.empty());
        when(documentRepository.save(any(SearchDocument.class))).thenAnswer(inv -> inv.getArgument(0));
        when(embeddingClient.embed(anyString())).thenThrow(new RuntimeException("down"));

        searchService.indexParsedFiles(repositoryId, "https://github.com/x/y.git", List.of(file));

        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }

    @Test
    void hybridSearch_withRepositoryId_usesScopedQuery() {
        when(embeddingClient.embed("auth")).thenReturn(List.of(0.1f));
        when(documentRepository.hybridSearchByRepository(eq("auth"), anyString(), eq(repositoryId)))
                .thenReturn(List.of());

        searchService.hybridSearch("auth", repositoryId);

        verify(documentRepository).hybridSearchByRepository(eq("auth"), anyString(), eq(repositoryId));
        verify(documentRepository, never()).hybridSearch(anyString(), anyString());
    }


    @Test
    void hybridSearch_withoutRepositoryId_usesGlobalQuery() {
        when(embeddingClient.embed("auth")).thenReturn(List.of(0.1f));
        when(documentRepository.hybridSearch(eq("auth"), anyString())).thenReturn(List.of());

        searchService.hybridSearch("auth", null);

        verify(documentRepository).hybridSearch(eq("auth"), anyString());
        verify(documentRepository, never()).hybridSearchByRepository(anyString(), anyString(), any());
    }
}
