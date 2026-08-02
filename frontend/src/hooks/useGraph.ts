import { useQuery } from "@tanstack/react-query";
import { api } from "../lib/api";

interface GraphFile {
    id: number;
    path: string;
    language: string;
}

interface GraphEdge {
    from: string;
    to: string;
}

export function useRepositoryGraph(repositoryId: string) {
    const filesQuery = useQuery({
        queryKey: ["graph-files", repositoryId],
        queryFn: () => api.get<{ files: GraphFile[] }>(`/api/v1/graph/repositories/${repositoryId}`),
    });

    const edgesQuery = useQuery({
        queryKey: ["graph-edges", repositoryId],
        queryFn: () => api.get<GraphEdge[]>(`/api/v1/graph/repositories/${repositoryId}/edges`),
    });

    return {
        files: filesQuery.data?.files ?? [],
        edges: edgesQuery.data ?? [],
        isLoading: filesQuery.isLoading || edgesQuery.isLoading,
        isError: filesQuery.isError || edgesQuery.isError,
    };
}