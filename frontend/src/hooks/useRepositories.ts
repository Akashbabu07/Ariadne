import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "../lib/api";
import type { RepositoryDto, RepositoryHealth } from "../lib/types";
import { useToastStore } from "../stores/toastStore";

export function useRepositories(projectId: string | undefined) {
    return useQuery({
        queryKey: ["repositories", projectId],
        queryFn: () => api.get<RepositoryDto[]>(`/api/v1/projects/${projectId}/repositories`),
        enabled: !!projectId,
    });
}

export function useRepositoryHealth(repositoryId: string) {
    return useQuery({
        queryKey: ["repository-health", repositoryId],
        queryFn: () => api.get<RepositoryHealth>(`/api/v1/analysis/repositories/${repositoryId}/health`),


        refetchInterval: 15_000,
        retry: false,
    });
}

export function useRegisterRepository(projectId: string | undefined) {
    const queryClient = useQueryClient();
    const push = useToastStore((s) => s.push);
    return useMutation({
        mutationFn: async (payload: { gitUrl: string; defaultBranch?: string }) => {
            const repo = await api.post<RepositoryDto>("/api/v1/repositories", { projectId, ...payload });
            await api.post("/api/v1/ingestion", { repositoryId: repo.id, gitUrl: repo.gitUrl });
            return repo;
        },
        onSuccess: (repo) => {
            queryClient.invalidateQueries({ queryKey: ["repositories", projectId] });
            push(`Registered ${repo.gitUrl} — ingestion started`, "success");
        },
        onError: () => push("Couldn't register that repository", "error"),
    });
}