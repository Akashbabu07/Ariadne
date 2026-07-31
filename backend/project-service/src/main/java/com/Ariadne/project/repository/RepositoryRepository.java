package com.Ariadne.project.repository;

import com.Ariadne.project.entity.Repository;
import com.Ariadne.project.entity.SyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface RepositoryRepository extends JpaRepository<Repository, UUID> {
    List<Repository> findByProjectId(UUID projectId);
    List<Repository> findBySyncStatusNotAndLastSyncedAtBeforeOrLastSyncedAtIsNull(
            SyncStatus excludedStatus, Instant cutoff);
}
