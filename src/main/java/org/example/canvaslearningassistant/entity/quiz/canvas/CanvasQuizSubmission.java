package org.example.canvaslearningassistant.entity.quiz.canvas;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CanvasQuizSubmission {

    @Id
    private Integer id; // Canvas submission ID

    private Integer attempt;

    private OffsetDateTime submittedAt;

    private Integer studentId;

    @ManyToOne
    @JoinColumn(name = "canvas_quiz_id")
    private CanvasQuiz quiz;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CanvasQuizSubmissionQuestion> questions;
}