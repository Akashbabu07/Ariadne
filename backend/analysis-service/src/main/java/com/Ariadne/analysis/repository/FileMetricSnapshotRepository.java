package com.Ariadne.analysis.repository;

import com.Ariadne.analysis.entity.FileMetricSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface FileMetricSnapshotRepository extends JpaRepository<FileMetricSnapshot, UUID> {

    @Query("""
        SELECT f FROM FileMetricSnapshot f
        WHERE f.repositoryId = :repositoryId
        AND f.capturedAt = (SELECT MAX(f2.capturedAt) FROM FileMetricSnapshot f2 WHERE f2.repositoryId = :repositoryId)
        """)
    List<FileMetricSnapshot> findLatestBatch(@Param("repositoryId") UUID repositoryId);
}