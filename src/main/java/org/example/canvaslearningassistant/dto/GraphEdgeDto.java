package org.example.canvaslearningassistant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GraphEdgeDto {

    @JsonProperty("node_1")
    private String firstNode;

    @JsonProperty("node_2")
    private String secondNode;
    private String edge;
}
