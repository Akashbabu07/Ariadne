FROM python:3.12-slim
WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

RUN python -c "from sentence_transformers import SentenceTransformer; SentenceTransformer('all-MiniLM-L6-v2')"

COPY . .
RUN python -m grpc_tools.protoc -I proto --python_out=. --grpc_python_out=. proto/embed.proto

EXPOSE 50052
CMD ["python", "server.py"]