package org.example.canvaslearningassistant.services.api;

import lombok.RequiredArgsConstructor;
import org.example.canvaslearningassistant.configuration.ExternalApiConfiguration;
import org.example.canvaslearningassistant.dto.AskRequest;
import org.example.canvaslearningassistant.dto.AskWithGraphRequest;
import org.example.canvaslearningassistant.dto.GraphEdgeDto;
import org.example.canvaslearningassistant.entity.GraphEdge;
import org.example.canvaslearningassistant.entity.StudentKnowledge;
import org.example.canvaslearningassistant.mappers.GraphEdgeMapper;
import org.example.canvaslearningassistant.services.api.interfaces.KnowledgeGraphApiService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeGraphApiServiceImpl implements KnowledgeGraphApiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ExternalApiConfiguration externalApiConfiguration;

    @Override
    public String ask(String userMessage, List<StudentKnowledge> knowledgeList) {

        final String API_URL = externalApiConfiguration.getQuizApiHost() + "/ask";

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

    @Override
    public String askWithGraph(
            String userMessage,
            List<StudentKnowledge> knowledgeList,
            List<GraphEdge> graphEdges
    ) {

        final String API_URL =
                externalApiConfiguration.getQuizApiHost() + "/ask-with-graph";

        Map<String, Integer> knowledgeMap = knowledgeList.stream()
                .collect(Collectors.toMap(
                        k -> k.getConcept().getName(),
                        StudentKnowledge::getMasteryLevel
                ));

        List<GraphEdgeDto> graphEdgeDtos = graphEdges.stream()
                .map(GraphEdgeMapper::toDto)
                .toList();

        AskWithGraphRequest request = new AskWithGraphRequest(
                userMessage,
                knowledgeMap,
                graphEdgeDtos
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AskWithGraphRequest> entity =
                new HttpEntity<>(request, headers);

        ResponseEntity<Map<String, String>> response =
                restTemplate.exchange(
                        API_URL,
                        HttpMethod.POST,
                        entity,
                        new ParameterizedTypeReference<>() {}
                );

        return response.getBody().get("response");
    }

    @Override
    public List<GraphEdgeDto> generateKnowledgeGraph(String userMessage) {
        final String API_URL = externalApiConfiguration.getQuizApiHost() + "/generate_graph";

        Map<String, Object> requestBody = Map.of(
                "text", userMessage
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

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