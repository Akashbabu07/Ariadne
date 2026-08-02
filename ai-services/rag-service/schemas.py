from pydantic import BaseModel
from typing import List, Optional
from uuid import UUID

class ContextRequest(BaseModel):
    repository_id: UUID
    query: str
    file_path: Optional[str] = None



class RetrievedChunk(BaseModel):
    file_path: str
    snippet: str
    source: str


class GraphContext(BaseModel):
    file_path: str
    dependencies: List[str]
    dependents: List[str]


class ContextResponse(BaseModel):
    query: str
    repository_id: UUID
    chunks: List[RetrievedChunk]
    graph_context: Optional[GraphContext] = None