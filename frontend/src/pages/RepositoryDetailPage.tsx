import { useParams } from "react-router-dom";
import { Tabs } from "../components/Tabs";
import { SearchPanel } from "../components/SearchPanel";
import { AskPanel } from "../components/AskPanel";
import { ImpactPanel } from "../components/ImpactPanel";
import { DriftPanel } from "../components/DriftPanel";
import { useRepositoryHealth } from "../hooks/useRepositories";
import { SyncStatusBadge, DriftBadge } from "../components/StatusBadge";
import { GraphView } from "../components/GraphView";

export default function RepositoryDetailPage() {
    const { repositoryId } = useParams<{ repositoryId: string }>();
    const { data: health } = useRepositoryHealth(repositoryId!);

    if (!repositoryId) return null;

    return (
        <div>
            <div className="mb-1 flex items-baseline gap-2">
                <span className="h-px w-4 bg-thread" />
                <p className="font-mono text-xs uppercase tracking-wide text-fog">Repository</p>
            </div>
            <div className="flex items-center gap-3">
                <h1 className="font-display text-2xl text-paper">Detail</h1>
                {health && <SyncStatusBadge status={health.syncStatus} />}
                {health && <DriftBadge detected={health.driftDetected} count={health.driftedFileCount} />}
            </div>

            {health && (
                <div className="mt-4 flex gap-6 font-mono text-sm text-fog">
                    <span>{health.fileCount} files</span>
                    <span>{health.dependencyEdgeCount} dependency edges</span>
                </div>
            )}

            <div className="mt-8">
                <Tabs
                    tabs={[
                        { label: "Search", content: <SearchPanel repositoryId={repositoryId} /> },
                        { label: "Ask Ariadne", content: <AskPanel repositoryId={repositoryId} /> },
                        { label: "Impact Analysis", content: <ImpactPanel repositoryId={repositoryId} /> },
                        { label: "Drift", content: <DriftPanel repositoryId={repositoryId} /> },
                        { label: "Graph", content: <GraphView repositoryId={repositoryId} /> },
                    ]}
                />
            </div>
        </div>
    );
}