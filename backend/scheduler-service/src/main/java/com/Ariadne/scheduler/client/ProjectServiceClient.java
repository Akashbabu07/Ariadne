package com.Ariadne.scheduler.client;

import com.Ariadne.scheduler.dto.RepositoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "project-service", url = "${services.project-service.url}")
public interface ProjectServiceClient {

    @GetMapping("/api/v1/projects/{projectId}/repositories")
    List<RepositoryResponse> listRepositories(@PathVariable("projectId") String projectId);
}
