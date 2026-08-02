
import os
import subprocess
import sys

SERVICE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PB2_FILE = os.path.join(SERVICE_DIR, "parser_pb2.py")

if not os.path.exists(PB2_FILE):
    subprocess.run(
        [
            sys.executable, "-m", "grpc_tools.protoc",
            "-I", "proto", "--python_out=.", "--grpc_python_out=.",
            "proto/parser.proto",
        ],
        cwd=SERVICE_DIR,
        check=True,
    )

sys.path.insert(0, SERVICE_DIR)