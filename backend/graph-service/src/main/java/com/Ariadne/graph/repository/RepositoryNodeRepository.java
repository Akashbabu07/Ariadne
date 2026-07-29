package com.Ariadne.graph.repository;

import com.Ariadne.graph.node.RepositoryNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Optional;

public interface RepositoryNodeRepository extends Neo4jRepository<RepositoryNode, String> {
    Optional<RepositoryNode> findByGitUrl(String gitUrl);
}