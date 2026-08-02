import sys, os
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import pytest
from unittest.mock import patch, AsyncMock
import retrieval


class FakeResponse:
    def __init__(self, json_data, status_code=200):
        self._json = json_data
        self.status_code = status_code

    def raise_for_status(self):
        if self.status_code >= 400:
            raise Exception(f"HTTP {self.status_code}")

    def json(self):
        return self._json


@pytest.mark.asyncio
async def test_fetch_hybrid_chunks_filters_out_incomplete_results():
    fake_results = {"data": [
        {"filePath": "src/a.py", "snippet": "def foo(): ..."},
        {"filePath": "src/b.py", "snippet": None},
        {"filePath": None, "snippet": "orphan"},
    ]}
    with patch("retrieval.httpx.AsyncClient") as mock_client_cls:
        mock_client = AsyncMock()
        mock_client.get.return_value = FakeResponse(fake_results)
        mock_client_cls.return_value.__aenter__.return_value = mock_client

        chunks = await retrieval.fetch_hybrid_chunks("repo-123", "how does auth work")

    assert len(chunks) == 1
    assert chunks[0].file_path == "src/a.py"
    assert chunks[0].source == "hybrid"


@pytest.mark.asyncio
async def test_fetch_hybrid_chunks_respects_limit():
    fake_results = {"data": [{"filePath": f"src/f{i}.py", "snippet": "x"} for i in range(20)]}
    with patch("retrieval.httpx.AsyncClient") as mock_client_cls:
        mock_client = AsyncMock()
        mock_client.get.return_value = FakeResponse(fake_results)
        mock_client_cls.return_value.__aenter__.return_value = mock_client

        chunks = await retrieval.fetch_hybrid_chunks("repo-123", "query", limit=5)

    assert len(chunks) == 5


@pytest.mark.asyncio
async def test_fetch_graph_context_returns_none_on_server_error():
    with patch("retrieval.httpx.AsyncClient") as mock_client_cls:
        mock_client = AsyncMock()
        mock_client.get.return_value = FakeResponse({}, status_code=500)
        mock_client_cls.return_value.__aenter__.return_value = mock_client

        result = await retrieval.fetch_graph_context("repo-123", "src/a.py")

    assert result is None