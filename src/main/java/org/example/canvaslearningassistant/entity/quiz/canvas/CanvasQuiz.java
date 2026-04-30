package org.example.canvaslearningassistant.entity.quiz.canvas;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import java.util.List;

@Entity
@Table(name = "canvas_quiz")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CanvasQuiz {

    @Id
    private Integer id; // Provided by Canvas API

    private Integer studentId;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CanvasQuizQuestion> questions;
}