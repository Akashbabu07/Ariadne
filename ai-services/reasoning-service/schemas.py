# schemas.py
from pydantic import BaseModel
from typing import List, Optional, Literal
from uuid import UUID


class ChunkInput(BaseModel):
    file_path: str
    snippet: str


class GraphContextInput(BaseModel):
    file_path: str
    dependencies: List[str] = []
    dependents: List[str] = []


class ReasoningRequest(BaseModel):
    repository_id: UUID
    mode: Literal["qa", "impact_analysis", "drift_explanation"]
    query: str
    chunks: List[ChunkInput] = []
    graph_context: Optional[GraphContextInput] = None


class ReasoningResponse(BaseModel):
    repository_id: UUID
    mode: str
    answer: str
    sources: List[str]