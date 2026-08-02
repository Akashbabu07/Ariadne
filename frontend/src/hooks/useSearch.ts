import { useMutation } from "@tanstack/react-query";
import { api } from "../lib/api";
import type { SearchResult } from "../lib/types";

export function useHybridSearch(repositoryId: string) {
    return useMutation({
        mutationFn: (query: string) =>
            api.get<SearchResult[]>(
                `/api/v1/search/hybrid?q=${encodeURIComponent(query)}&repositoryId=${repositoryId}`
            ),
    });
}