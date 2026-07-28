package com.Ariadne.ingestion.repository;

import com.Ariadne.ingestion.entity.IngestionJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IngestionJobRepository extends JpaRepository<IngestionJob, UUID> {
}