package org.example.canvaslearningassistant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "graph_edges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "node_1", nullable = false)
    private Concept firstNode;

    @ManyToOne
    @JoinColumn(name = "node_2", nullable = false)
    private Concept secondNode;

    @Column(nullable = false)
    private String edge;

    @Column(name = "course_id", nullable = false)
    private Integer courseId;
}