export interface Organization {
    id: string;
    name: string;
    createdAt: string;
}

export interface Project {
    id: string;
    orgId: string;
    name: string;
    description: string | null;
    createdAt: string;
}

export interface RepositoryDto {
    id: string;
    projectId: string;
    gitUrl: string;
    defaultBranch: string;
    syncStatus: "PENDING" | "SYNCING" | "SYNCED" | "FAILED";
    lastSyncedAt: string | null;
    createdAt: string;
}

export interface RepositoryHealth {
    repositoryId: string;
    syncStatus: string;
    lastSyncedAt: string | null;
    fileCount: number;
    dependencyEdgeCount: number;
    lastAnalysisAt: string | null;
    driftDetected: boolean;
    driftedFileCount: number;
    lastDriftCheckAt: string | null;
}

export interface SearchResult {
    repositoryId: string;
    gitUrl: string;
    filePath: string;
    snippet: string | null;
    indexedAt: string;
}

export interface AskResponse {
    repositoryId: string;
    mode: "qa" | "impact_analysis" | "drift_explanation";
    answer: string;
    sources: string[];
}

export interface DriftedFile {
    path: string;
    prevFanIn: number;
    newFanIn: number;
    prevFanOut: number;
    newFanOut: number;
}

export interface User {
    id: string;
    orgId: string;
    email: string;
    status: string;
    roles: string[];
}