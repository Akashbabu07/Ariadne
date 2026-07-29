package com.Ariadne.graph.service;

import com.Ariadne.graph.dto.*;
import com.Ariadne.graph.node.FileNode;
import com.Ariadne.graph.node.RepositoryNode;
import com.Ariadne.graph.repository.RepositoryNodeRepository;
import com.Ariadne.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GraphService {

    private final RepositoryNodeRepository repositoryNodeRepository;

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

    private RepositoryGraphResponse toResponse(RepositoryNode node) {
        return new RepositoryGraphResponse(
                node.getId(), node.getGitUrl(),
                node.getFiles().stream()
                        .map(f -> new RepositoryGraphResponse.FileResponse(f.getId(), f.getPath(), f.getLanguage()))
                        .toList()
        );
    }
}