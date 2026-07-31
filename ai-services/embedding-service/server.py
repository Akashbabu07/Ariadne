import grpc
from concurrent import futures
from sentence_transformers import SentenceTransformer

import embed_pb2
import embed_pb2_grpc

_model = SentenceTransformer("all-MiniLM-L6-v2")


class EmbeddingServiceServicer(embed_pb2_grpc.EmbeddingServiceServicer):
    def Embed(self, request, context):
        vector = _model.encode(request.text).tolist()
        return embed_pb2.EmbedResponse(vector=vector, dimensions=len(vector))


def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    embed_pb2_grpc.add_EmbeddingServiceServicer_to_server(EmbeddingServiceServicer(), server)
    server.add_insecure_port("[::]:50052")
    server.start()
    print("embedding-service gRPC server running on port 50052")
    server.wait_for_termination()


if __name__ == "__main__":
    serve()
