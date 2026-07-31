package com.Ariadne.analysis.client;

import com.Ariadne.analysis.dto.RepositoryGraphResponse;
import com.Ariadne.shared.response.ApiEnvelope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "graph-service", url = "${services.graph-service.url}")
public interface GraphServiceClient {

    @GetMapping("/api/v1/graph/repositories/{repositoryId}")
    ApiEnvelope<RepositoryGraphResponse> getRepositoryGraph(@PathVariable("repositoryId") String repositoryId);
}
