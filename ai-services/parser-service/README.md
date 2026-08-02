# parser-service (Python, gRPC)

Minimal gRPC server proving the Java (ai-gateway) <-> Python wire contract works.
Real Tree-sitter AST parsing is not implemented yet — ParseRepository returns fake data.

## Setup
```
pip install -r requirements.txt
python -m grpc_tools.protoc -I proto --python_out=. --grpc_python_out=. proto/parser.proto
python server.py
```
This generates `parser_pb2.py` and `parser_pb2_grpc.py` from `proto/parser.proto`, then starts
the gRPC server on port 50051, matching `ai-gateway`'s `application.yaml` client config.
