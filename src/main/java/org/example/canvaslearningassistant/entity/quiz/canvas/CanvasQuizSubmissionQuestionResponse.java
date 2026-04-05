package org.example.canvaslearningassistant.entity.quiz.canvas;

import jakarta.persistence.*;
import lombok.*;
import org.example.canvaslearningassistant.entity.quiz.canvas.CanvasQuizQuestion;
import org.example.canvaslearningassistant.entity.quiz.canvas.CanvasQuizQuestionAnswer;
import org.example.canvaslearningassistant.entity.quiz.canvas.CanvasQuizSubmission;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CanvasQuizSubmissionQuestionResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "question_id", referencedColumnName = "question_id"),
            @JoinColumn(name = "answer_id", referencedColumnName = "answer_id")
    })
    private CanvasQuizQuestionAnswer answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private CanvasQuizSubmission submission;
}