import os
import httpx
from typing import Optional
from schemas import RetrievedChunk, GraphContext

SEARCH_SERVICE_URL = f"http://{os.getenv('SEARCH_SERVICE_HOST', 'localhost')}:{os.getenv('SEARCH_SERVICE_PORT', '8086')}"
GRAPH_SERVICE_URL = f"http://{os.getenv('GRAPH_SERVICE_HOST', 'localhost')}:{os.getenv('GRAPH_SERVICE_PORT', '8085')}"

async def fetch_hybrid_chunks(repository_id: str, query: str, limit: int = 8) -> list[RetrievedChunk]:
    async with httpx.AsyncClient(timeout=10.0) as client:
        resp = await client.get(
            f"{SEARCH_SERVICE_URL}/api/v1/search/hybrid",
            params={"q": query, "repositoryId": repository_id},
        )
        resp.raise_for_status()
        results = resp.json()["data"]

    return [
        RetrievedChunk(file_path=r["filePath"], snippet=r["snippet"], source="hybrid")
        for r in results[:limit]
        if r.get("filePath") and r.get("snippet")
    ]


async def fetch_graph_context(repository_id: str, file_path: str) -> Optional[GraphContext]:
    async with httpx.AsyncClient(timeout=10.0) as client:
        deps_resp = await client.get(
            f"{GRAPH_SERVICE_URL}/api/v1/graph/repositories/{repository_id}/dependencies",
            params={"path": file_path},
        )
        dependents_resp = await client.get(
            f"{GRAPH_SERVICE_URL}/api/v1/graph/repositories/{repository_id}/dependents",
            params={"path": file_path},
        )

    if deps_resp.status_code != 200 or dependents_resp.status_code != 200:
        return None

    deps = [f["path"] for f in deps_resp.json()["data"]]
    dependents = [f["path"] for f in dependents_resp.json()["data"]]
    return GraphContext(file_path=file_path, dependencies=deps, dependents=dependents)