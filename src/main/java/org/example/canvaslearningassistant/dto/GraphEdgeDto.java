package org.example.canvaslearningassistant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GraphEdgeDto {

    @JsonProperty("node_1")
    private final String firstNode;

    @JsonProperty("node_2")
    private final String secondNode;
    private final String edge;
}
