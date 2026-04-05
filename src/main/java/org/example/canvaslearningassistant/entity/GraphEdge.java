package org.example.canvaslearningassistant.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "graph_edges")
public class GraphEdge implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // primary key

    @Column(name = "node_1", nullable = false)
    private String firstNode;

    @Column(name = "node_2", nullable = false)
    private String secondNode;

    @Column(nullable = false)
    private String edge;

    @Column(name = "course_id", nullable = false)
    private Integer courseId;
}