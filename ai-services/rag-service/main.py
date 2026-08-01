# main.py
from fastapi import FastAPI, HTTPException
from schemas import ContextRequest, ContextResponse
import retrieval

app = FastAPI(title="rag-service", version="0.1.0")


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/api/v1/rag/context", response_model=ContextResponse)
async def build_context(request: ContextRequest):
    chunks = await retrieval.fetch_hybrid_chunks(str(request.repository_id), request.query)

    if not chunks and not request.file_path:
        raise HTTPException(
            status_code=404,
            detail="No indexed content found for this repository/query — has it finished ingesting?",
        )

    target_file = request.file_path or (chunks[0].file_path if chunks else None)
    graph_context = await retrieval.fetch_graph_context(str(request.repository_id), target_file) if target_file else None

    return ContextResponse(
        query=request.query,
        repository_id=request.repository_id,
        chunks=chunks,
        graph_context=graph_context,
    )