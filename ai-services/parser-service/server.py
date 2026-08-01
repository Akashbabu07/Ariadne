import os
import tempfile
import shutil
import grpc
from concurrent import futures
from git import Repo

from tree_sitter import Language, Parser
import tree_sitter_python as tspython
import tree_sitter_javascript as tsjavascript

import parser_pb2
import parser_pb2_grpc

LANGUAGE_BY_EXT = {
    ".py": "python", ".java": "java", ".js": "javascript", ".ts": "typescript",
    ".go": "go", ".rs": "rust", ".md": "markdown", ".yml": "yaml", ".yaml": "yaml",
    ".json": "json", ".sql": "sql", ".html": "html", ".css": "css",
}
IGNORED_DIRS = {".git", "node_modules", "target", "venv", "__pycache__", "dist", "build"}

PY_LANGUAGE = Language(tspython.language())
JS_LANGUAGE = Language(tsjavascript.language())

_py_parser = Parser(PY_LANGUAGE)
_js_parser = Parser(JS_LANGUAGE)


_PY_IMPORT_QUERY = PY_LANGUAGE.query("""
(import_statement (dotted_name) @import)
(import_from_statement module_name: (dotted_name) @import)
""")

_JS_IMPORT_QUERY = JS_LANGUAGE.query("""
(import_statement source: (string (string_fragment) @import))
(call_expression
  function: (identifier) @fn
  arguments: (arguments (string (string_fragment) @import))
  (#eq? @fn "require"))
""")


def extract_imports(source_bytes: bytes, language: str) -> list[str]:
    try:
        if language == "python":
            tree = _py_parser.parse(source_bytes)
            captures = _PY_IMPORT_QUERY.captures(tree.root_node)
            nodes = captures.get("import", [])
            return sorted({source_bytes[n.start_byte:n.end_byte].decode("utf-8", "ignore") for n in nodes})
        if language in ("javascript", "typescript"):
            tree = _js_parser.parse(source_bytes)
            captures = _JS_IMPORT_QUERY.captures(tree.root_node)
            nodes = captures.get("import", [])
            return sorted({source_bytes[n.start_byte:n.end_byte].decode("utf-8", "ignore") for n in nodes})
    except Exception:
        return []
    return []


def clone_and_walk(git_url: str):
    tmp_dir = tempfile.mkdtemp(prefix="ariadne_parse_")
    try:
        Repo.clone_from(git_url, tmp_dir, depth=1)
        files = []
        for root, dirs, filenames in os.walk(tmp_dir):
            dirs[:] = [d for d in dirs if d not in IGNORED_DIRS]
            for fname in filenames:
                ext = os.path.splitext(fname)[1]
                if ext not in LANGUAGE_BY_EXT:
                    continue
                full_path = os.path.join(root, fname)
                rel_path = os.path.relpath(full_path, tmp_dir)
                language = LANGUAGE_BY_EXT[ext]

                with open(full_path, "rb") as f:
                    raw = f.read()
                line_count = raw.count(b"\n") + 1

                imports = extract_imports(raw, language)
                content = raw.decode("utf-8", "ignore")[:8000]
                files.append({
                    "path": rel_path.replace("\\", "/"),
                    "language": language,
                    "line_count": line_count,
                    "imports": imports,
                     "content": content,
                })
        return files
    finally:
        shutil.rmtree(tmp_dir, ignore_errors=True)


class ParserServiceServicer(parser_pb2_grpc.ParserServiceServicer):
    def ParseRepository(self, request, context):
        try:
            parsed = clone_and_walk(request.git_url)
        except Exception as e:
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(str(e))
            return parser_pb2.ParseResponse()

        files = [
            parser_pb2.ParsedFile(
                path=f["path"], language=f["language"],
                line_count=f["line_count"], imports=f["imports"],
                content=f["content"],
            )
            for f in parsed
        ]
        return parser_pb2.ParseResponse(
            repository_id=request.repository_id,
            files_parsed=len(files),
            files=files,
        )


def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    parser_pb2_grpc.add_ParserServiceServicer_to_server(ParserServiceServicer(), server)
    server.add_insecure_port("[::]:50051")
    server.start()
    print("parser-service gRPC server running on port 50051")
    server.wait_for_termination()


if __name__ == "__main__":
    serve()
