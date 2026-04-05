package org.example.canvaslearningassistant.entity.quiz.canvas;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CanvasQuizQuestionAnswerId implements Serializable {

    @Column(name = "question_id")
    private Integer questionId;

    @Column(name = "answer_id")
    private Integer answerId;
}