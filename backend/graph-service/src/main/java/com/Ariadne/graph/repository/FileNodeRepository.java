package com.Ariadne.graph.repository;

import com.Ariadne.graph.node.FileNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;
import java.util.Optional;

public interface FileNodeRepository extends Neo4jRepository<FileNode, Long> {

    @Query("""
        MATCH (:Repository {id: $repositoryId})-[:CONTAINS]->(f:File {path: $path})
        RETURN f
        """)
    Optional<FileNode> findByRepositoryIdAndPath(String repositoryId, String path);


    @Query("""
        MATCH (:Repository {id: $repositoryId})-[:CONTAINS]->(:File {path: $path})-[:DEPENDS_ON]->(dep:File)
        RETURN dep
        """)
    List<FileNode> findDependencies(String repositoryId, String path);


    @Query("""
        MATCH (:Repository {id: $repositoryId})-[:CONTAINS]->(dep:File {path: $path})
        MATCH (f:File)-[:DEPENDS_ON]->(dep)
        RETURN f
        """)
    List<FileNode> findDependents(String repositoryId, String path);


    @Query("""
        MATCH (:Repository {id: $repositoryId})-[:CONTAINS]->(from:File {path: $fromPath})
        MATCH (:Repository {id: $repositoryId})-[:CONTAINS]->(to:File {path: $toPath})
        MERGE (from)-[:DEPENDS_ON]->(to)
        """)
    void addDependency(String repositoryId, String fromPath, String toPath);

    @Query("""
        MATCH (:Repository {id: $repositoryId})-[:CONTAINS]->(f:File)
        WHERE f.path CONTAINS $suffix
        RETURN f
        LIMIT 1
        """)
    Optional<FileNode> findByRepositoryIdAndPathSuffix(String repositoryId, String suffix);
}
