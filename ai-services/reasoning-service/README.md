# README.md
# reasoning-service (Python, FastAPI + LangGraph)

Performs multistep reasoning over already-retrieved context (from rag-service)
using a local Ollama model. Never re-fetches or reparses repository data —
consumes only what's passed in, per the platform's separation of concerns.

Modes: `qa` (repository Q&A), `impact_analysis`, `drift_explanation`.

## Setup