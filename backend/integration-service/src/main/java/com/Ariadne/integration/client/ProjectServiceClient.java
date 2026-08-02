package com.Ariadne.integration.client;

import com.Ariadne.integration.dto.RepositoryStatusResponse;
import com.Ariadne.shared.response.ApiEnvelope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "project-service", url = "${services.project-service.url}")
public interface ProjectServiceClient {

    @GetMapping("/api/v1/repositories/by-git-url")
    ApiEnvelope<RepositoryStatusResponse> findByGitUrl(@RequestParam("gitUrl") String gitUrl);
}