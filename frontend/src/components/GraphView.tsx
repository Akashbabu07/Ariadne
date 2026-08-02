import { useMemo, useState } from "react";
import ReactFlow, { Background, Controls, MiniMap } from "reactflow";
import "reactflow/dist/style.css";
import { useRepositoryGraph } from "../hooks/useGraph";
import { layoutGraph } from "../lib/graphLayout";

export function GraphView({ repositoryId }: { repositoryId: string }) {
    const { files, edges, isLoading, isError } = useRepositoryGraph(repositoryId);
    const [highlightPath, setHighlightPath] = useState("");

    const highlighted = useMemo(() => {
        if (!highlightPath) return new Set<string>();
        // Simple client-side neighborhood highlight: the typed file plus anything
        // one hop away in either direction — enough to trace a thread visually
        // without re-running a full server-side impact analysis just to highlight.
        const set = new Set<string>([highlightPath]);
        edges.forEach((e) => {
            if (e.from === highlightPath) set.add(e.to);
            if (e.to === highlightPath) set.add(e.from);
        });
        return set;
    }, [highlightPath, edges]);

    const { nodes, edges: flowEdges } = useMemo(
        () => layoutGraph(files, edges, highlighted),
        [files, edges, highlighted]
    );

    if (isLoading) return <p className="text-sm text-fog">Loading graph…</p>;
    if (isError) return <p className="text-sm text-risk">Couldn't load the dependency graph.</p>;
    if (files.length === 0) {
        return <p className="text-sm text-fog">No graph data yet — parsing may still be in progress.</p>;
    }

    return (
        <div>
            <input
                value={highlightPath}
                onChange={(e) => setHighlightPath(e.target.value)}
                placeholder="Trace a file's direct connections…"
                className="mb-3 w-full max-w-md rounded-md border border-line bg-surface px-3 py-2 font-mono text-xs text-paper outline-none focus:border-thread"
            />
            <div style={{ height: 520 }} className="overflow-hidden rounded-lg border border-line">
                <ReactFlow nodes={nodes} edges={flowEdges} fitView proOptions={{ hideAttribution: true }}>
                    <Background color="var(--line)" gap={20} />
                    <Controls showInteractive={false} />
                    <MiniMap
                        style={{ background: "var(--surface)" }}
                        nodeColor="var(--line)"
                        maskColor="rgba(18,20,28,0.7)"
                    />
                </ReactFlow>
            </div>
        </div>
    );
}