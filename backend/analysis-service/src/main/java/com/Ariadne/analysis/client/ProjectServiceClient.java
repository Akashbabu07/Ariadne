package com.Ariadne.analysis.client;

import com.Ariadne.analysis.dto.RepositoryStatusResponse;
import com.Ariadne.shared.response.ApiEnvelope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "project-service", url = "${services.project-service.url}")
public interface ProjectServiceClient {

    @GetMapping("/api/v1/repositories/{repositoryId}")
    ApiEnvelope<RepositoryStatusResponse> getRepository(@PathVariable("repositoryId") String repositoryId);
}