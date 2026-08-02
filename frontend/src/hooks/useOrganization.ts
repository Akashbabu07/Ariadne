import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "../lib/api";
import type { Organization, Project } from "../lib/types";

export function useOrganization(orgId: string | undefined) {
    return useQuery({
        queryKey: ["organization", orgId],
        queryFn: () => api.get<Organization>(`/api/v1/organizations/${orgId}`),
        enabled: !!orgId,
    });
}

export function useProjects(orgId: string | undefined) {
    return useQuery({
        queryKey: ["projects", orgId],
        queryFn: () => api.get<Project[]>(`/api/v1/organizations/${orgId}/projects`),
        enabled: !!orgId,
    });
}

export function useCreateProject(orgId: string | undefined) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (payload: { name: string; description?: string }) =>
            api.post<Project>("/api/v1/projects", { orgId, ...payload }),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ["projects", orgId] }),
    });
}