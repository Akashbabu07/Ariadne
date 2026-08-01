from fastapi import FastAPI
from schemas import ReasoningRequest, ReasoningResponse
from graph import run_reasoning

app = FastAPI(title="reasoning-service", version="0.1.0")


@app.get("/health")
def health():
    return {"status": "ok"}

@app.post("/api/v1/reasoning/ask", response_model=ReasoningResponse)
def ask(request: ReasoningRequest):
    context_text = _format_context(request)
    available_sources = [c.file_path for c in request.chunks]
    if request.graph_context:
        available_sources.append(request.graph_context.file_path)

    result = run_reasoning(request.mode, request.query, context_text, available_sources)

    return ReasoningResponse(
        repository_id=request.repository_id,
        mode=request.mode,
        answer=result["answer"],
        sources=result["sources"],
    )


def _format_context(request: ReasoningRequest) -> str:
    parts = [f"--- {c.file_path} ---\n{c.snippet}" for c in request.chunks]

    if request.graph_context:
        gc = request.graph_context
        parts.append(
            f"--- dependency graph for {gc.file_path} ---\n"
            f"Depends on: {', '.join(gc.dependencies) or 'none'}\n"
            f"Depended on by: {', '.join(gc.dependents) or 'none'}"
        )

    return "\n\n".join(parts) if parts else "No context available."