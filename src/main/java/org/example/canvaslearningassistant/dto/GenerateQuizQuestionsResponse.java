package org.example.canvaslearningassistant.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GenerateQuizQuestionsResponse {
    private List<GenerateQuizQuestionsItemResponse> questions;
}
