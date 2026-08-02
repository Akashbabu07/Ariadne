import {type FormEvent, useState } from "react";
import { useAsk } from "../hooks/useAsk";

export function AskPanel({ repositoryId }: { repositoryId: string }) {
    const [query, setQuery] = useState("");
    const ask = useAsk(repositoryId);

    function handleSubmit(e: FormEvent) {
        e.preventDefault();
        if (query.trim()) ask.mutate({ mode: "qa", query });
    }

    return (
        <div>
            <form onSubmit={handleSubmit} className="flex gap-2">
                <input
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    placeholder="Ask a question about this repository…"
                    className="flex-1 rounded-md border border-line bg-surface px-3 py-2 text-sm text-paper outline-none focus:border-thread"
                />
                <button
                    type="submit"
                    disabled={ask.isPending}
                    className="rounded-md bg-thread px-4 py-2 text-sm font-medium text-ink disabled:opacity-50"
                >
                    {ask.isPending ? "Thinking…" : "Ask"}
                </button>
            </form>

            {ask.isError && (
                <p className="mt-4 text-sm text-risk">
                    Couldn't get an answer — check rag-service and reasoning-service are running.
                </p>
            )}

            {ask.data && (
                <div className="mt-4 rounded-lg border border-line bg-surface p-4">
                    <p className="whitespace-pre-wrap text-sm text-paper">{ask.data.answer}</p>
                    {ask.data.sources.length > 0 && (
                        <div className="mt-3 flex flex-wrap gap-1.5 border-t border-line pt-3">
                            {ask.data.sources.map((s) => (
                                <span key={s} className="rounded border border-line px-2 py-0.5 font-mono text-xs text-fog">
                  {s}
                </span>
                            ))}
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}