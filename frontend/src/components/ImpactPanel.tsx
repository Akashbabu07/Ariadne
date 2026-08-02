import {type FormEvent, useState } from "react";
import { useRunImpactAnalysis } from "../hooks/useAnalysis";

export function ImpactPanel({ repositoryId }: { repositoryId: string }) {
    const [path, setPath] = useState("");
    const impact = useRunImpactAnalysis(repositoryId);

    function handleSubmit(e: FormEvent) {
        e.preventDefault();
        if (path.trim()) impact.mutate(path);
    }

    return (
        <div>
            <p className="mb-3 text-sm text-fog">
                Enter a file path to see what depends on it, transitively, and what to review before changing it.
            </p>
            <form onSubmit={handleSubmit} className="flex gap-2">
                <input
                    value={path}
                    onChange={(e) => setPath(e.target.value)}
                    placeholder="src/main/java/com/example/AuthService.java"
                    className="flex-1 rounded-md border border-line bg-surface px-3 py-2 font-mono text-sm text-paper outline-none focus:border-thread"
                />
                <button
                    type="submit"
                    disabled={impact.isPending}
                    className="rounded-md bg-thread px-4 py-2 text-sm font-medium text-ink disabled:opacity-50"
                >
                    {impact.isPending ? "Analyzing…" : "Analyze"}
                </button>
            </form>

            {impact.data && (
                <div className="mt-4 space-y-3">
                    <div className="rounded-lg border border-line bg-surface p-4">
                        <p className="text-sm text-paper">{impact.data.explanation}</p>
                    </div>
                    {impact.data.impactedFiles.length > 0 && (
                        <div>
                            <p className="mb-2 text-xs uppercase tracking-wide text-fog">
                                {impact.data.impactedFiles.length} file(s) affected
                            </p>
                            <div className="space-y-1">
                                {impact.data.impactedFiles.map((f) => (
                                    <div key={f} className="flex items-center gap-2 font-mono text-xs text-fog">
                                        <span className="h-px w-3 bg-thread" />
                                        {f}
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}