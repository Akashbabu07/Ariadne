import { useMutation } from "@tanstack/react-query";
import { api } from "../lib/api";
import type { DriftedFile } from "../lib/types";
import { useToastStore } from "../stores/toastStore";

interface ImpactResult {
    id: string;
    repositoryId: string;
    targetPath: string;
    impactedFiles: string[];
    explanation: string;
    createdAt: string;
}

interface DriftResult {
    id: string;
    repositoryId: string;
    driftedFiles: DriftedFile[];
    explanation: string | null;
    createdAt: string;
}

export function useRunImpactAnalysis(repositoryId: string) {
    return useMutation({
        mutationFn: (path: string) =>
            api.post<ImpactResult>(
                `/api/v1/analysis/repositories/${repositoryId}/impact?path=${encodeURIComponent(path)}`
            ),
    });
}


export function useRunDriftDetection(repositoryId: string) {
    const push = useToastStore((s) => s.push);
    return useMutation({
        mutationFn: () => api.post<DriftResult>(`/api/v1/analysis/repositories/${repositoryId}/drift`),
        onSuccess: (result) => {
            push(
                result.driftedFiles.length === 0
                    ? "No drift detected — structure is stable"
                    : `Drift detected in ${result.driftedFiles.length} file(s)`,
                result.driftedFiles.length === 0 ? "success" : "error"
            );
        },
        onError: () => push("Drift check failed — is analysis-service running?", "error"),
    });
}