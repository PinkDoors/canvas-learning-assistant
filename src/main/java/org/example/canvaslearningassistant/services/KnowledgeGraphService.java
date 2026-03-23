package org.example.canvaslearningassistant.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.canvaslearningassistant.entities.GraphEdge;
import org.example.canvaslearningassistant.repository.GraphEdgeRepository;
import org.example.canvaslearningassistant.services.api.CanvasApiService;
import org.example.canvaslearningassistant.services.api.KnowledgeGraphApiService;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeGraphService {

    private final CanvasApiService canvasApiService;
    private final KnowledgeGraphApiService knowledgeGraphApiService;
    private final GraphEdgeRepository graphEdgeRepository;

    public List<GraphEdge> getGraph(Integer courseId, String accessToken) {

        // Check if graph exists
        List<GraphEdge> existingGraph = graphEdgeRepository.findByCourseId(courseId);
        if (!existingGraph.isEmpty()) {
            log.info("Found existing graph in PostgreSQL for course {}", courseId);
            return existingGraph;
        }

        log.info("No existing graph found. Calling KnowledgeGraph API for course {}", courseId);
        var courseDescription = canvasApiService.getFrontPageContent(courseId, accessToken);
        var graphEdges = knowledgeGraphApiService.generateKnowledgeGraph(courseDescription);

        if (graphEdges.isEmpty()) {
            log.warn("KnowledgeGraph API returned no edges for course {}", courseId);
            return List.of();
        }

        List<GraphEdge> entities = graphEdges.stream()
                .map(dto -> new GraphEdge(null, dto.getFirstNode(), dto.getSecondNode(), dto.getEdge(), courseId))
                .toList();

        // Save all edges
        List<GraphEdge> savedEdges = graphEdgeRepository.saveAll(entities);
        log.info("Saved {} new edges for course {}", savedEdges.size(), courseId);

        return savedEdges;
    }
}
