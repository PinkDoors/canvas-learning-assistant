package org.example.canvaslearningassistant.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewQuizQuestionData {

    private String question;

    private QuizQuestionType type;

    private List<String> options;

    private List<Integer> correctIndexes;

    private String concept;
}