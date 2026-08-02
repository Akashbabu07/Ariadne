package com.Ariadne.analysis.listener;

import com.Ariadne.AnalysisService;
import com.Ariadne.shared.events.GraphUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GraphUpdatedListener {

    private final AnalysisService analysisService;

    @KafkaListener(topics = "graph.updated", groupId = "analysis-service")
    public void onGraphUpdated(GraphUpdatedEvent event) {
        analysisService.runBasicAnalysis(event.repositoryId());
    }
}