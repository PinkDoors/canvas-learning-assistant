package org.example.canvaslearningassistant.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GenerateQuizQuestionsItemResponse {

    private String question;
    private String type; // "multiple_choice", "true_false", "multiple_answers"
    private List<String> options;
    private List<Integer> correct_index; // can be Integer or List<Integer>
    private String concept;
}