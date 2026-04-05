package org.example.canvaslearningassistant.mappers;

import org.example.canvaslearningassistant.dto.GraphEdgeDto;
import org.example.canvaslearningassistant.entity.GraphEdge;

public class GraphEdgeMapper {
    public static GraphEdgeDto toDto(GraphEdge graphEdge) {
        return new GraphEdgeDto(
                graphEdge.getFirstNode().getName(),
                graphEdge.getSecondNode().getName(),
                graphEdge.getEdge()
        );
    }
}
