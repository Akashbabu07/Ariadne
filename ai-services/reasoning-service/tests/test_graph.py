
import sys, os
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from unittest.mock import patch, MagicMock
import graph


def test_extract_sources_node_finds_only_cited_paths():
    state = {
        "answer": "The issue is in src/auth/Login.java, which doesn't validate tokens.",
        "available_sources": ["src/auth/Login.java", "src/util/Logger.java"],
    }
    result = graph.extract_sources_node(state)
    assert result["cited_sources"] == ["src/auth/Login.java"]


def test_should_retry_when_nothing_cited_and_context_was_available():
    state = {"cited_sources": [], "available_sources": ["a.py"], "retry_count": 0}
    assert graph.should_retry(state) == "retry"


def test_should_retry_stops_after_one_retry_to_avoid_infinite_loop():
    state = {"cited_sources": [], "available_sources": ["a.py"], "retry_count": 1}
    assert graph.should_retry(state) == "done"


def test_should_retry_done_when_no_context_was_ever_available():
    state = {"cited_sources": [], "available_sources": [], "retry_count": 0}
    assert graph.should_retry(state) == "done"


def test_should_retry_done_when_something_was_cited():
    state = {"cited_sources": ["a.py"], "available_sources": ["a.py", "b.py"], "retry_count": 0}
    assert graph.should_retry(state) == "done"


@patch("graph._llm")
def test_generate_node_calls_llm_with_correct_system_prompt(mock_llm):
    mock_llm.invoke.return_value = MagicMock(content="mocked answer")
    state = {
        "mode": "qa", "query": "how does auth work?",
        "context_text": "--- src/auth/Login.java ---\n...",
        "available_sources": ["src/auth/Login.java"],
        "answer": "", "cited_sources": [], "retry_count": 0,
    }
    result = graph.generate_node(state)

    assert result["answer"] == "mocked answer"
    mock_llm.invoke.assert_called_once()
    messages = mock_llm.invoke.call_args[0][0]
    assert "Ariadne" in messages[0].content