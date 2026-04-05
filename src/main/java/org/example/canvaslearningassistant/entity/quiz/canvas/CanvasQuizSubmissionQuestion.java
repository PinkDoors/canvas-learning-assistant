package org.example.canvaslearningassistant.entity.quiz.canvas;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CanvasQuizSubmissionQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Question reference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private CanvasQuizQuestion question;

    // ✅ Selected answers (many-to-many)
    @ManyToMany
    @JoinTable(
            name = "submission_question_selected_answers",
            joinColumns = @JoinColumn(name = "submission_question_id"),
            inverseJoinColumns = {
                    @JoinColumn(name = "question_id", referencedColumnName = "question_id"),
                    @JoinColumn(name = "answer_id", referencedColumnName = "answer_id")
            }
    )
    private List<CanvasQuizQuestionAnswer> selectedAnswers;

    // Submission reference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private CanvasQuizSubmission submission;
}