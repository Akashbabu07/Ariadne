import {type FormEvent, useState } from "react";
import { useHybridSearch } from "../hooks/useSearch";

export function SearchPanel({ repositoryId }: { repositoryId: string }) {
    const [query, setQuery] = useState("");
    const search = useHybridSearch(repositoryId);

    function handleSubmit(e: FormEvent) {
        e.preventDefault();
        if (query.trim()) search.mutate(query);
    }

    return (
        <div>
            <form onSubmit={handleSubmit} className="flex gap-2">
                <input
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    placeholder="Search this repository's code…"
                    className="flex-1 rounded-md border border-line bg-surface px-3 py-2 text-sm text-paper outline-none focus:border-thread"
                />
                <button type="submit" className="rounded-md bg-thread px-4 py-2 text-sm font-medium text-ink">
                    Search
                </button>
            </form>

            {search.isPending && <p className="mt-4 text-sm text-fog">Searching…</p>}

            {search.data?.length === 0 && (
                <p className="mt-4 text-sm text-fog">No matches — try a different term, or check indexing has finished.</p>
            )}

            <div className="mt-4 space-y-2">
                {search.data?.map((r, i) => (
                    <div key={i} className="rounded-lg border border-line bg-surface p-3">
                        <p className="font-mono text-xs text-thread">{r.filePath}</p>
                        {r.snippet && (
                            <pre className="mt-2 overflow-x-auto whitespace-pre-wrap font-mono text-xs text-fog">
                {r.snippet.slice(0, 300)}
              </pre>
                        )}
                    </div>
                ))}
            </div>
        </div>
    );
}