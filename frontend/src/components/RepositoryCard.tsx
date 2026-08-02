import { Link } from "react-router-dom";
import { useRepositoryHealth } from "../hooks/useRepositories";
import { SyncStatusBadge, DriftBadge } from "./StatusBadge";
import { Skeleton } from "./Skeleton";
import type { RepositoryDto } from "../lib/types";

export function RepositoryCard({ repo }: { repo: RepositoryDto }) {
    const { data: health } = useRepositoryHealth(repo.id);

    const repoName = repo.gitUrl
        .replace(/^https?:\/\/(www\.)?github\.com\//, "")
        .replace(/\.git$/, "");

    return (
        <Link
            to={`/repositories/${repo.id}`}
            className="block rounded-lg border border-line bg-surface p-4 transition hover:border-thread/50"
        >
            <div className="flex items-start justify-between gap-4">
                <div className="min-w-0">
                    <p className="truncate font-mono text-sm text-paper">
                        {repoName}
                    </p>
                    <p className="mt-0.5 text-xs text-fog">
                        {repo.defaultBranch}
                    </p>
                </div>

                <SyncStatusBadge status={repo.syncStatus} />
            </div>

            <div className="mt-4 flex items-center gap-4 text-xs text-fog">
                {health ? (
                    <>
                        <span>{health.fileCount} files</span>
                        <span>{health.dependencyEdgeCount} edges</span>
                        <DriftBadge
                            detected={health.driftDetected}
                            count={health.driftedFileCount}
                        />
                    </>
                ) : (
                    <>
                        <Skeleton className="h-3 w-14" />
                        <Skeleton className="h-3 w-14" />
                    </>
                )}
            </div>
        </Link>
    );
}