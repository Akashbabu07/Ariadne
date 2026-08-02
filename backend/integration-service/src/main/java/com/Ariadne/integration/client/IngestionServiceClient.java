package com.Ariadne.integration.client;

import com.Ariadne.integration.dto.StartIngestionRequest;
import com.Ariadne.shared.response.ApiEnvelope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ingestion-service", url = "${services.ingestion-service.url}")
public interface IngestionServiceClient {

    @PostMapping("/api/v1/ingestion")
    ApiEnvelope<Object> startIngestion(@RequestBody StartIngestionRequest request);
}