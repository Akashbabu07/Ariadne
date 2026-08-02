// service/AiGatewayService.java — add the ask() method
package com.Ariadne.aigateway.service;

import com.Ariadne.aigateway.client.ParserClient;
import com.Ariadne.aigateway.client.RagServiceClient;
import com.Ariadne.aigateway.client.ReasoningServiceClient;
import com.Ariadne.aigateway.dto.*;
import com.Ariadne.grpc.parser.ParseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiGatewayService {

    private final ParserClient parserClient;
    private final RagServiceClient ragServiceClient;
    private final ReasoningServiceClient reasoningServiceClient;

    public ParseResultResponse triggerParse(String repositoryId, String gitUrl) {
        ParseResponse response = parserClient.parseRepository(repositoryId, gitUrl);
        return new ParseResultResponse(
                response.getRepositoryId(),
                response.getFilesParsed(),
                response.getFilesList().stream()
                        .map(f -> new ParseResultResponse.ParsedFile(
                                f.getPath(), f.getLanguage(), f.getLineCount(), f.getImportsList()))
                        .toList()
        );
    }

    public AskResponse ask(AskRequest request) {
        RagContextResponse context = ragServiceClient.fetchContext(
                request.repositoryId(), request.query(), request.filePath());

        ReasoningResultDto result = reasoningServiceClient.reason(
                request.repositoryId(), request.mode(), request.query(), context);

        return new AskResponse(result.repositoryId(), result.mode(), result.answer(), result.sources());
    }
}