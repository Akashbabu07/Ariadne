package com.Ariadne.graph.service;

import com.Ariadne.graph.dto.IngestParsedFilesRequest;
import com.Ariadne.graph.node.FileNode;
import com.Ariadne.graph.node.RepositoryNode;
import com.Ariadne.graph.repository.FileNodeRepository;
import com.Ariadne.graph.repository.RepositoryNodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GraphServiceTest {
    @Mock private RepositoryNodeRepository repositoryNodeRepository;
    @Mock private FileNodeRepository fileNodeRepository;

    private GraphService graphService;
    private final String repositoryId = "repo-123";

    @BeforeEach
    void setUp() {
        graphService = new GraphService(repositoryNodeRepository, fileNodeRepository);
    }


    @Test
    void ingestParsedFiles_resolvesImportBySuffixMatch() {
        RepositoryNode node = RepositoryNode.builder().id(repositoryId).gitUrl("https://x").build();
        when(repositoryNodeRepository.findById(repositoryId)).thenReturn(Optional.of(node));
        when(repositoryNodeRepository.save(any())).thenReturn(node);

        FileNode targetFile = FileNode.builder().id(1L).path("src/main/java/com/foo/Bar.java").language("java").build();
        when(fileNodeRepository.findByRepositoryIdAndPathSuffix(repositoryId, "Bar"))
                .thenReturn(Optional.of(targetFile));

        var entry = new IngestParsedFilesRequest.ParsedFileEntry(
                "src/main/java/com/foo/Consumer.java", "java", List.of("com.foo.Bar"));

        graphService.ingestParsedFiles(repositoryId, new IngestParsedFilesRequest(List.of(entry)));

        verify(fileNodeRepository).addDependency(
                repositoryId, "src/main/java/com/foo/Consumer.java", "src/main/java/com/foo/Bar.java");
    }

    @Test
    void ingestParsedFiles_doesNotCreateSelfDependency() {
        RepositoryNode node = RepositoryNode.builder().id(repositoryId).gitUrl("https://x").build();
        when(repositoryNodeRepository.findById(repositoryId)).thenReturn(Optional.of(node));
        when(repositoryNodeRepository.save(any())).thenReturn(node);


        FileNode selfFile = FileNode.builder().id(1L).path("src/main/Self.java").language("java").build();
        when(fileNodeRepository.findByRepositoryIdAndPathSuffix(repositoryId, "Self"))
                .thenReturn(Optional.of(selfFile));

        var entry = new IngestParsedFilesRequest.ParsedFileEntry(
                "src/main/Self.java", "java", List.of("com.foo.Self"));

        graphService.ingestParsedFiles(repositoryId, new IngestParsedFilesRequest(List.of(entry)));

        verify(fileNodeRepository, never()).addDependency(anyString(), anyString(), anyString());
    }


    @Test
    void ingestParsedFiles_noSuffixMatch_skipsSilentlyWithoutError() {
        RepositoryNode node = RepositoryNode.builder().id(repositoryId).gitUrl("https://x").build();
        when(repositoryNodeRepository.findById(repositoryId)).thenReturn(Optional.of(node));
        when(repositoryNodeRepository.save(any())).thenReturn(node);
        when(fileNodeRepository.findByRepositoryIdAndPathSuffix(eq(repositoryId), anyString()))
                .thenReturn(Optional.empty());

        var entry = new IngestParsedFilesRequest.ParsedFileEntry(
                "src/main/Consumer.java", "java", List.of("external.library.Unknown"));

        graphService.ingestParsedFiles(repositoryId, new IngestParsedFilesRequest(List.of(entry)));

        verify(fileNodeRepository, never()).addDependency(anyString(), anyString(), anyString());
    }
    @Test
    void ingestParsedFiles_addsFileNodesThatDoNotAlreadyExist() {
        RepositoryNode node = RepositoryNode.builder().id(repositoryId).gitUrl("https://x").build();
        when(repositoryNodeRepository.findById(repositoryId)).thenReturn(Optional.of(node));
        when(repositoryNodeRepository.save(any())).thenReturn(node);

        var entry = new IngestParsedFilesRequest.ParsedFileEntry("src/main/New.java", "java", List.of());

        graphService.ingestParsedFiles(repositoryId, new IngestParsedFilesRequest(List.of(entry)));

        boolean added = node.getFiles().stream().anyMatch(f -> f.getPath().equals("src/main/New.java"));
        org.assertj.core.api.Assertions.assertThat(added).isTrue();
    }
    @Test
    void ingestParsedFiles_existingFileNotDuplicated() {
        RepositoryNode node = RepositoryNode.builder().id(repositoryId).gitUrl("https://x").build();
        node.getFiles().add(FileNode.builder().id(1L).path("src/main/Existing.java").language("java").build());
        when(repositoryNodeRepository.findById(repositoryId)).thenReturn(Optional.of(node));
        when(repositoryNodeRepository.save(any())).thenReturn(node);

        var entry = new IngestParsedFilesRequest.ParsedFileEntry("src/main/Existing.java", "java", List.of());

        graphService.ingestParsedFiles(repositoryId, new IngestParsedFilesRequest(List.of(entry)));

        long count = node.getFiles().stream().filter(f -> f.getPath().equals("src/main/Existing.java")).count();
        org.assertj.core.api.Assertions.assertThat(count).isEqualTo(1);
    }

}