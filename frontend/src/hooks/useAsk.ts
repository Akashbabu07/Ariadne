import { useMutation } from "@tanstack/react-query";
import { api } from "../lib/api";
import type { AskResponse } from "../lib/types";

export function useAsk(repositoryId: string) {
    return useMutation({
        mutationFn: (payload: { mode: AskResponse["mode"]; query: string; filePath?: string }) =>
            api.post<AskResponse>("/api/v1/ai/ask", { repositoryId, ...payload }),
    });
}