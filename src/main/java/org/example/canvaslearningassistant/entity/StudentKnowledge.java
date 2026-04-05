package org.example.canvaslearningassistant.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_knowledge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentKnowledge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer studentId;

    @ManyToOne
    @JoinColumn(name = "concept_id", nullable = false)
    private Concept concept;

    private Integer masteryLevel;
}