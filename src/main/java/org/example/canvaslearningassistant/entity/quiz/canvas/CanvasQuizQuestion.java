package org.example.canvaslearningassistant.entity.quiz.canvas;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import java.util.List;

@Entity
@Table(name = "canvas_quiz_question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CanvasQuizQuestion {

    @Id
    private Integer id; // Provided by Canvas API

    private String questionName;
    private String questionText;
    private String questionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private CanvasQuiz quiz;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CanvasQuizQuestionAnswer> answers;
}