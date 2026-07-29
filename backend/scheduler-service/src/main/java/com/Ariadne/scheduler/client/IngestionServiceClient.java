package com.Ariadne.scheduler.client;

import com.Ariadne.scheduler.dto.IngestionJobResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ingestion-service", url = "${services.ingestion-service.url}")
public interface IngestionServiceClient {

    @PostMapping("/api/v1/ingestion")
    IngestionJobResponse startIngestion(@RequestBody StartIngestionRequest request);

    record StartIngestionRequest(String repositoryId, String gitUrl) {}
}