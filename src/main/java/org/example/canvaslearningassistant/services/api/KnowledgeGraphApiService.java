package org.example.canvaslearningassistant.services.api;

import lombok.RequiredArgsConstructor;
import org.example.canvaslearningassistant.configuration.ExternalApiConfiguration;
import org.example.canvaslearningassistant.dto.AskRequest;
import org.example.canvaslearningassistant.dto.GraphEdgeDto;
import org.example.canvaslearningassistant.entity.StudentKnowledge;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeGraphApiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ExternalApiConfiguration externalApiConfiguration;

    public String ask(String userMessage, List<StudentKnowledge> knowledgeList) {

        final String API_URL = externalApiConfiguration.getQuizApiHost() + "/ask";

        // convert to map: concept -> mastery
        Map<String, Integer> knowledgeMap = knowledgeList.stream()
                .collect(Collectors.toMap(
                        k -> k.getConcept().getName(),
                        StudentKnowledge::getMasteryLevel
                ));

        AskRequest request = new AskRequest(
                userMessage,
                knowledgeMap
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AskRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map<String, String>> response =
                restTemplate.exchange(
                        API_URL,
                        HttpMethod.POST,
                        entity,
                        new ParameterizedTypeReference<>() {}
                );

        return response.getBody().get("response");
    }

    public List<GraphEdgeDto> generateKnowledgeGraph(String userMessage) {
        final String API_URL = externalApiConfiguration.getQuizApiHost() + "/generate_graph";

        // Build request body
        Map<String, Object> requestBody = Map.of(
                "text", userMessage
        );

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // POST request
        ResponseEntity<List<GraphEdgeDto>> response =
                restTemplate.exchange(
                        API_URL,
                        HttpMethod.POST,
                        entity,
                        new ParameterizedTypeReference<>() {
                        }
                );

        return response.getBody();
    }
}