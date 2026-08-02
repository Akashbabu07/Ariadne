from typing import TypedDict, List
from langgraph.graph import StateGraph, END
from langchain_core.messages import SystemMessage, HumanMessage
from prompts import system_prompt_for
from llm_provider import build_llm

_llm = build_llm()


class ReasoningState(TypedDict):
    mode: str
    query: str
    context_text: str
    available_sources: List[str]
    answer: str
    cited_sources: List[str]
    retry_count: int


def generate_node(state: ReasoningState) -> ReasoningState:
    messages = [
        SystemMessage(content=system_prompt_for(state["mode"])),
        HumanMessage(content=f"{state['context_text']}\n\nQuestion: {state['query']}"),
    ]
    response = _llm.invoke(messages)
    return {**state, "answer": response.content}


def extract_sources_node(state: ReasoningState) -> ReasoningState:
    cited = [path for path in state["available_sources"] if path in state["answer"]]
    return {**state, "cited_sources": cited}


def should_retry(state: ReasoningState) -> str:
    if not state["cited_sources"] and state["available_sources"] and state["retry_count"] < 1:
        return "retry"
    return "done"


def retry_generate_node(state: ReasoningState) -> ReasoningState:
    messages = [
        SystemMessage(content=system_prompt_for(state["mode"])),
        HumanMessage(content=(
            f"{state['context_text']}\n\nQuestion: {state['query']}\n\n"
            "Your previous answer didn't reference any specific file from the "
            "context. Rewrite it and explicitly name the relevant file path(s)."
        )),
    ]
    response = _llm.invoke(messages)
    return {**state, "answer": response.content, "retry_count": state["retry_count"] + 1}


def build_graph():
    graph = StateGraph(ReasoningState)
    graph.add_node("generate", generate_node)
    graph.add_node("extract_sources", extract_sources_node)
    graph.add_node("retry_generate", retry_generate_node)

    graph.set_entry_point("generate")
    graph.add_edge("generate", "extract_sources")
    graph.add_conditional_edges("extract_sources", should_retry, {
        "retry": "retry_generate",
        "done": END,
    })
    graph.add_edge("retry_generate", END)

    return graph.compile()


_compiled_graph = build_graph()


def run_reasoning(mode: str, query: str, context_text: str, available_sources: List[str]) -> dict:
    initial_state: ReasoningState = {
        "mode": mode, "query": query, "context_text": context_text,
        "available_sources": available_sources, "answer": "",
        "cited_sources": [], "retry_count": 0,
    }
    final_state = _compiled_graph.invoke(initial_state)
    return {"answer": final_state["answer"], "sources": final_state["cited_sources"]}