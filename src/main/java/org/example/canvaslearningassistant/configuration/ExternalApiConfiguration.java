package org.example.canvaslearningassistant.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class ExternalApiConfiguration {

    @Value("${canvas-api.host}")
    private String canvasApiHost;

    @Value("${quiz-api.host}")
    private String quizApiHost;

    @Value("${knowledge-graph-api.host}")
    private String knowledgeGraphApiHost;
}
