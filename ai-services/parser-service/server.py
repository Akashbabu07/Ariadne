import grpc
from concurrent import futures
import parser_pb2
import parser_pb2_grpc


class ParserServiceServicer(parser_pb2_grpc.ParserServiceServicer):
    def ParseRepository(self, request, context):

        fake_files = [
            parser_pb2.ParsedFile(path="src/main.py", language="python", line_count=42),
            parser_pb2.ParsedFile(path="README.md", language="markdown", line_count=10),
        ]
        return parser_pb2.ParseResponse(
            repository_id=request.repository_id,
            files_parsed=len(fake_files),
            files=fake_files,
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
