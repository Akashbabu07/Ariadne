package com.Ariadne.analysis.client;

import com.Ariadne.analysis.dto.FileMetricsResponse;
import com.Ariadne.analysis.dto.RepositoryGraphResponse;
import com.Ariadne.shared.response.ApiEnvelope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "graph-service", url = "${services.graph-service.url}")
public interface GraphServiceClient {

    @GetMapping("/api/v1/graph/repositories/{repositoryId}")
    ApiEnvelope<RepositoryGraphResponse> getRepositoryGraph(@PathVariable("repositoryId") String repositoryId);


    @GetMapping("/api/v1/graph/repositories/{repositoryId}/impact")
    ApiEnvelope<java.util.List<RepositoryGraphResponse.FileResponse>> getTransitiveDependents(
            @PathVariable("repositoryId") String repositoryId, @RequestParam("path") String path);

    @GetMapping("/api/v1/graph/repositories/{repositoryId}/metrics")
    ApiEnvelope<java.util.List<FileMetricsResponse>> getFileMetrics(@PathVariable("repositoryId") String repositoryId);
}
