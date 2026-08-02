package com.Ariadne.aigateway.client;

import com.Ariadne.grpc.parser.ParseRequest;
import com.Ariadne.grpc.parser.ParseResponse;
import com.Ariadne.grpc.parser.ParserServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
public class ParserClient {

    @GrpcClient("parser-service")
    private ParserServiceGrpc.ParserServiceBlockingStub parserStub;

    public ParseResponse parseRepository(String repositoryId, String gitUrl) {
        ParseRequest request = ParseRequest.newBuilder()
                .setRepositoryId(repositoryId)
                .setGitUrl(gitUrl)
                .build();
        return parserStub.parseRepository(request);
    }
}
