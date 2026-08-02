package com.Ariadne.search.client;

import com.Ariadne.grpc.embedding.EmbedRequest;
import com.Ariadne.grpc.embedding.EmbedResponse;
import com.Ariadne.grpc.embedding.EmbeddingServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmbeddingClient {

    @GrpcClient("embedding-service")
    private EmbeddingServiceGrpc.EmbeddingServiceBlockingStub embeddingStub;

    public List<Float> embed(String text) {
        EmbedResponse response = embeddingStub.embed(EmbedRequest.newBuilder().setText(text).build());
        return response.getVectorList();
    }
}
