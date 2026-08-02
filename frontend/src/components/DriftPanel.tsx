import { useRunDriftDetection } from "../hooks/useAnalysis";

export function DriftPanel({ repositoryId }: { repositoryId: string }) {
    const drift = useRunDriftDetection(repositoryId);

    return (
        <div>
            <div className="flex items-center justify-between">
                <p className="text-sm text-fog">
                    Compares current coupling (fan-in/fan-out) against the last check. The first run
                    establishes a baseline — nothing will be flagged yet.
                </p>
                <button
                    onClick={() => drift.mutate()}
                    disabled={drift.isPending}
                    className="shrink-0 rounded-md bg-thread px-4 py-2 text-sm font-medium text-ink disabled:opacity-50"
                >
                    {drift.isPending ? "Checking…" : "Run drift check"}
                </button>
            </div>

            {drift.data && (
                <div className="mt-4">
                    {drift.data.driftedFiles.length === 0 ? (
                        <p className="rounded-lg border border-safe/40 bg-surface p-4 text-sm text-safe">
                            No drift detected — structure is stable since the last check.
                        </p>
                    ) : (
                        <div className="space-y-3">
                            {drift.data.explanation && (
                                <div className="rounded-lg border border-risk/40 bg-surface p-4">
                                    <p className="text-sm text-paper">{drift.data.explanation}</p>
                                </div>
                            )}
                            <table className="w-full text-left text-xs">
                                <thead>
                                <tr className="border-b border-line text-fog">
                                    <th className="pb-2 font-normal">File</th>
                                    <th className="pb-2 font-normal">Fan-in</th>
                                    <th className="pb-2 font-normal">Fan-out</th>
                                </tr>
                                </thead>
                                <tbody className="font-mono">
                                {drift.data.driftedFiles.map((f) => (
                                    <tr key={f.path} className="border-b border-line/50">
                                        <td className="py-2 text-paper">{f.path}</td>
                                        <td className="py-2 text-fog">{f.prevFanIn} → {f.newFanIn}</td>
                                        <td className="py-2 text-fog">{f.prevFanOut} → {f.newFanOut}</td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}