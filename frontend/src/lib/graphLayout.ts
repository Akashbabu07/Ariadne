import dagre from "dagre";
import type { Node, Edge } from "reactflow";

interface GraphFile {
    id: number;
    path: string;
    language: string;
}

interface GraphEdge {
    from: string;
    to: string;
}

const NODE_WIDTH = 220;
const NODE_HEIGHT = 44;

export function layoutGraph(
    files: GraphFile[],
    edges: GraphEdge[],
    highlightedPaths: Set<string> = new Set()
): { nodes: Node[]; edges: Edge[] } {
    const g = new dagre.graphlib.Graph();
    g.setDefaultEdgeLabel(() => ({}));
    g.setGraph({ rankdir: "LR", nodesep: 24, ranksep: 80 });

    files.forEach((f) => g.setNode(f.path, { width: NODE_WIDTH, height: NODE_HEIGHT }));
    edges.forEach((e) => g.setEdge(e.from, e.to));

    dagre.layout(g);

    const nodes: Node[] = files.map((f) => {
        const pos = g.node(f.path);
        const isHighlighted = highlightedPaths.has(f.path);
        const shortName = f.path.split("/").pop() ?? f.path;

        return {
            id: f.path,
            position: { x: pos?.x ?? 0, y: pos?.y ?? 0 },
            data: { label: shortName, fullPath: f.path, language: f.language },
            style: {
                width: NODE_WIDTH,
                background: "var(--surface)",
                border: `1px solid ${isHighlighted ? "var(--thread)" : "var(--line)"}`,
                borderRadius: 6,
                color: isHighlighted ? "var(--thread)" : "var(--paper)",
                fontFamily: "JetBrains Mono, monospace",
                fontSize: 12,
                padding: "8px 10px",
            },
        };
    });

    const flowEdges: Edge[] = edges.map((e, i) => {
        const highlighted = highlightedPaths.has(e.from) && highlightedPaths.has(e.to);
        return {
            id: `${e.from}->${e.to}-${i}`,
            source: e.from,
            target: e.to,
            style: { stroke: highlighted ? "var(--thread)" : "var(--line)", strokeWidth: highlighted ? 2 : 1 },
            animated: highlighted,
        };
    });

    return { nodes, edges: flowEdges };
}