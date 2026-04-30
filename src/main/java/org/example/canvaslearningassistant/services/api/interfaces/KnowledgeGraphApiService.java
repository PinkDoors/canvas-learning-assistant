package org.example.canvaslearningassistant.services.api.interfaces;

import org.example.canvaslearningassistant.dto.GraphEdgeDto;
import org.example.canvaslearningassistant.entity.GraphEdge;
import org.example.canvaslearningassistant.entity.StudentKnowledge;

import java.util.List;

public interface KnowledgeGraphApiService {
    String ask(String userMessage, List<StudentKnowledge> knowledgeList);

    String askWithGraph(
            String userMessage,
            List<StudentKnowledge> knowledgeList,
            List<GraphEdge> graphEdges
    );

    List<GraphEdgeDto> generateKnowledgeGraph(String userMessage);
}
