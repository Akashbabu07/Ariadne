package com.Ariadne.aigateway.service;

import com.Ariadne.aigateway.client.ParserClient;
import com.Ariadne.aigateway.client.RagServiceClient;
import com.Ariadne.aigateway.client.ReasoningServiceClient;
import com.Ariadne.aigateway.dto.AskRequest;
import com.Ariadne.aigateway.dto.AskResponse;
import com.Ariadne.aigateway.dto.RagContextResponse;
import com.Ariadne.aigateway.dto.ReasoningResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiGatewayServiceTest {

    @Mock private ParserClient parserClient;
    @Mock private RagServiceClient ragServiceClient;
    @Mock private ReasoningServiceClient reasoningServiceClient;

    private AiGatewayService aiGatewayService;
    private final UUID repositoryId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        aiGatewayService = new AiGatewayService(parserClient, ragServiceClient, reasoningServiceClient);
    }

    @Test
    void ask_fetchesContextThenPassesItToReasoning() {
        AskRequest request = new AskRequest(repositoryId, "qa", "how does auth work?", null);

        RagContextResponse context = new RagContextResponse(
                "how does auth work?", repositoryId,
                List.of(new RagContextResponse.ChunkDto("src/auth/Login.java", "public class Login {...}", "hybrid")),
                null
        );
        when(ragServiceClient.fetchContext(repositoryId, "how does auth work?", null)).thenReturn(context);

        ReasoningResultDto reasoningResult = new ReasoningResultDto(
                repositoryId, "qa", "Auth is handled in Login.java via JWT validation.", List.of("src/auth/Login.java"));
        when(reasoningServiceClient.reason(repositoryId, "qa", "how does auth work?", context))
                .thenReturn(reasoningResult);

        AskResponse response = aiGatewayService.ask(request);

        assertThat(response.repositoryId()).isEqualTo(repositoryId);
        assertThat(response.mode()).isEqualTo("qa");
        assertThat(response.answer()).isEqualTo("Auth is handled in Login.java via JWT validation.");
        assertThat(response.sources()).containsExactly("src/auth/Login.java");

        verify(ragServiceClient).fetchContext(repositoryId, "how does auth work?", null);
        verify(reasoningServiceClient).reason(repositoryId, "qa", "how does auth work?", context);
    }

    @Test
    void ask_passesFilePathThroughToRagWhenProvided() {
        String filePath = "src/main/AuthService.java";
        AskRequest request = new AskRequest(repositoryId, "impact_analysis", "what breaks if this changes?", filePath);

        RagContextResponse context = new RagContextResponse(
                "what breaks if this changes?", repositoryId, List.of(), null);
        when(ragServiceClient.fetchContext(repositoryId, "what breaks if this changes?", filePath)).thenReturn(context);
        when(reasoningServiceClient.reason(eq(repositoryId), eq("impact_analysis"), anyString(), eq(context)))
                .thenReturn(new ReasoningResultDto(repositoryId, "impact_analysis", "answer", List.of()));

        aiGatewayService.ask(request);

        verify(ragServiceClient).fetchContext(repositoryId, "what breaks if this changes?", filePath);
    }

    @Test
    void ask_ragReturnsEmptyChunks_stillCallsReasoningRatherThanShortCircuiting() {

        AskRequest request = new AskRequest(repositoryId, "qa", "unrelated question", null);
        RagContextResponse emptyContext = new RagContextResponse("unrelated question", repositoryId, List.of(), null);
        when(ragServiceClient.fetchContext(repositoryId, "unrelated question", null)).thenReturn(emptyContext);
        when(reasoningServiceClient.reason(eq(repositoryId), eq("qa"), anyString(), eq(emptyContext)))
                .thenReturn(new ReasoningResultDto(repositoryId, "qa", "Not enough information.", List.of()));

        AskResponse response = aiGatewayService.ask(request);

        verify(reasoningServiceClient).reason(any(), any(), any(), any());
        assertThat(response.answer()).isEqualTo("Not enough information.");
    }
}