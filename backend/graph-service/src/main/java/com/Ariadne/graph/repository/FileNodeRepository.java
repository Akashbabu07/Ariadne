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

    @Query("""
    MATCH (:Repository {id: $repositoryId})-[:CONTAINS]->(:File {path: $path})-[:DEPENDS_ON*1..5]->(dep:File)
    RETURN DISTINCT dep
    """)
    List<FileNode> findTransitiveDependencies(String repositoryId, String path);

    @Query("""
    MATCH (:Repository {id: $repositoryId})-[:CONTAINS]->(target:File {path: $path})
    MATCH (dep:File)-[:DEPENDS_ON*1..5]->(target)
    RETURN DISTINCT dep
    """)
    List<FileNode> findTransitiveDependents(String repositoryId, String path);

    @Query("""
    MATCH (:Repository {id: $repositoryId})-[:CONTAINS]->(f:File)
    OPTIONAL MATCH (f)-[out:DEPENDS_ON]->()
    OPTIONAL MATCH (f)<-[in:DEPENDS_ON]-()
    RETURN f.path AS path, f.language AS language, count(DISTINCT in) AS fanIn, count(DISTINCT out) AS fanOut
    """)
    List<FileMetricsProjection> findFileMetrics(String repositoryId);


    @Query("""
    MATCH (:Repository {id: $repositoryId})-[:CONTAINS]->(f:File)-[:DEPENDS_ON]->(dep:File)
    RETURN f.path AS from, dep.path AS to
    """)
    List<DependencyEdgeProjection> findAllEdges(String repositoryId);
}
