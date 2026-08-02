const SYNC_STYLES: Record<string, string> = {
    PENDING: "text-fog border-line",
    SYNCING: "text-thread border-thread/40",
    SYNCED: "text-safe border-safe/40",
    FAILED: "text-risk border-risk/40",
};

export function SyncStatusBadge({ status }: { status: string }) {
    return (
        <span className={`rounded border px-2 py-0.5 font-mono text-xs ${SYNC_STYLES[status] ?? SYNC_STYLES.PENDING}`}>
      {status}
    </span>
    );
}

export function DriftBadge({ detected, count }: { detected: boolean; count: number }) {
    if (!detected) {
        return <span className="rounded border border-safe/40 px-2 py-0.5 font-mono text-xs text-safe">stable</span>;
    }
    return (
        <span className="rounded border border-risk/40 px-2 py-0.5 font-mono text-xs text-risk">
      drift · {count} file{count === 1 ? "" : "s"}
    </span>
    );
}