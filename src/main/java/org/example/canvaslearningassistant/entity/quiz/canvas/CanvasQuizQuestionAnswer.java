package org.example.canvaslearningassistant.entity.quiz.canvas;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "canvas_quiz_question_answer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CanvasQuizQuestionAnswer {

    @EmbeddedId
    private CanvasQuizQuestionAnswerId id;

    private String text;
    private Integer weight;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questionId")
    @JoinColumn(name = "question_id")
    private CanvasQuizQuestion question;
}