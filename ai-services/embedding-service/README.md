# embedding-service (Python, gRPC)

Produces 384-dimension sentence embeddings using `all-MiniLM-L6-v2` (Sentence Transformers).
Called by search-service to generate vectors for semantic search over indexed repositories.

## Setup
```
pip install -r requirements.txt
python -m grpc_tools.protoc -I proto --python_out=. --grpc_python_out=. proto/embed.proto
python server.py
```
Starts the gRPC server on port 50052. First run will download the model (~90MB) from
Hugging Face — requires internet access once, then it's cached locally.
