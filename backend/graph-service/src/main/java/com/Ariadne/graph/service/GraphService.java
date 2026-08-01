package com.Ariadne.graph.service;

import com.Ariadne.graph.dto.*;
import com.Ariadne.graph.node.FileNode;
import com.Ariadne.graph.node.RepositoryNode;
import com.Ariadne.graph.repository.FileNodeRepository;
import com.Ariadne.graph.repository.RepositoryNodeRepository;
import com.Ariadne.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GraphService {

    private final RepositoryNodeRepository repositoryNodeRepository;
    private final FileNodeRepository fileNodeRepository;

    @Transactional
    public RepositoryGraphResponse createRepositoryNode(CreateRepositoryNodeRequest request) {
        RepositoryNode node = repositoryNodeRepository.findById(request.repositoryId().toString())
                .orElseGet(() -> RepositoryNode.builder()
                        .id(request.repositoryId().toString())
                        .gitUrl(request.gitUrl())
                        .build());
        node = repositoryNodeRepository.save(node);
        return toResponse(node);
    }

    @Transactional
    public RepositoryGraphResponse addFile(String repositoryId, AddFileRequest request) {
        RepositoryNode node = repositoryNodeRepository.findById(repositoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository node not found in graph"));

        FileNode file = FileNode.builder().path(request.path()).language(request.language()).build();
        node.getFiles().add(file);
        node = repositoryNodeRepository.save(node);
        return toResponse(node);
    }

    public RepositoryGraphResponse getRepositoryGraph(String repositoryId) {
        RepositoryNode node = repositoryNodeRepository.findById(repositoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository node not found in graph"));
        return toResponse(node);
    }


    @Transactional
    public void addDependency(String repositoryId, AddDependencyRequest request) {
        fileNodeRepository.findByRepositoryIdAndPath(repositoryId, request.fromPath())
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + request.fromPath()));
        fileNodeRepository.findByRepositoryIdAndPath(repositoryId, request.toPath())
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + request.toPath()));
        fileNodeRepository.addDependency(repositoryId, request.fromPath(), request.toPath());
    }

    public List<RepositoryGraphResponse.FileResponse> getDependencies(String repositoryId, String path) {
        return fileNodeRepository.findDependencies(repositoryId, path).stream()
                .map(this::toFileResponse)
                .toList();
    }

    public List<RepositoryGraphResponse.FileResponse> getDependents(String repositoryId, String path) {
        return fileNodeRepository.findDependents(repositoryId, path).stream()
                .map(this::toFileResponse)
                .toList();
    }


    @Transactional
    public int  ingestParsedFiles(String repositoryId, IngestParsedFilesRequest request) {
        RepositoryNode node = repositoryNodeRepository.findById(repositoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository node not found in graph"));

        for (IngestParsedFilesRequest.ParsedFileEntry entry : request.files()) {
            boolean exists = node.getFiles().stream().anyMatch(f -> f.getPath().equals(entry.path()));
            if (!exists) {
                node.getFiles().add(FileNode.builder().path(entry.path()).language(entry.language()).build());
            }
        }
        repositoryNodeRepository.save(node);

        int edgesCreated = 0;
        for (IngestParsedFilesRequest.ParsedFileEntry entry : request.files()) {
            if (entry.imports() == null) continue;
            for (String rawImport : entry.imports()) {
                String lastSegment = lastSegment(rawImport);
                if (lastSegment.isBlank()) continue;

                Optional<FileNode> target = fileNodeRepository.findByRepositoryIdAndPathSuffix(repositoryId, lastSegment);
                if (target.isPresent() && !target.get().getPath().equals(entry.path())) {
                    fileNodeRepository.addDependency(repositoryId, entry.path(), target.get().getPath());
                    edgesCreated++;
                }
            }
        }
        return edgesCreated;
    }

    private String lastSegment(String rawImport) {
        String cleaned = rawImport.replace("./", "").replace("../", "");
        String[] parts = cleaned.split("[./]");
        return parts.length == 0 ? "" : parts[parts.length - 1];
    }

    private RepositoryGraphResponse.FileResponse toFileResponse(FileNode f) {
        return new RepositoryGraphResponse.FileResponse(f.getId(), f.getPath(), f.getLanguage());
    }

    private RepositoryGraphResponse toResponse(RepositoryNode node) {
        return new RepositoryGraphResponse(
                node.getId(), node.getGitUrl(),
                node.getFiles().stream()
                        .map(this::toFileResponse)
                        .toList()
        );
    }

    public List<RepositoryGraphResponse.FileResponse> getTransitiveDependencies(String repositoryId, String path) {
        return fileNodeRepository.findTransitiveDependencies(repositoryId, path).stream()
                .map(this::toFileResponse)
                .toList();
    }

    public List<RepositoryGraphResponse.FileResponse> getTransitiveDependents(String repositoryId, String path) {
        return fileNodeRepository.findTransitiveDependents(repositoryId, path).stream()
                .map(this::toFileResponse)
                .toList();
    }

    public List<FileMetricsResponse> getFileMetrics(String repositoryId) {
        return fileNodeRepository.findFileMetrics(repositoryId).stream()
                .map(p -> new FileMetricsResponse(p.getPath(), p.getLanguage(), p.getFanIn(), p.getFanOut()))
                .toList();
    }
}