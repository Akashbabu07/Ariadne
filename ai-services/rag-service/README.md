# README.md
# rag-service (Python, FastAPI)

Assembles retrieval context for LLM reasoning by combining:
- hybrid (keyword + vector) search results from search-service
- dependency/dependent context from graph-service

Does not call an LLM itself — consumed by ai-gateway, which forwards the
assembled context to reasoning-service. Does not reparse or re-embed
repositories; purely reads already-indexed data.

## Setup