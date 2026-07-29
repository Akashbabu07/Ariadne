package com.Ariadne.aigateway.service;

import com.Ariadne.aigateway.client.ParserClient;
import com.Ariadne.aigateway.dto.ParseResultResponse;
import com.Ariadne.grpc.parser.ParseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiGatewayService {

    private final ParserClient parserClient;

    public ParseResultResponse triggerParse(String repositoryId, String gitUrl) {
        ParseResponse response = parserClient.parseRepository(repositoryId, gitUrl);
        return new ParseResultResponse(
                response.getRepositoryId(),
                response.getFilesParsed(),
                response.getFilesList().stream().map(f -> f.getPath()).toList()
        );
    }
}
