FROM python:3.12-slim
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends git && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .
RUN python -m grpc_tools.protoc -I proto --python_out=. --grpc_python_out=. proto/parser.proto

EXPOSE 50051
CMD ["python", "server.py"]