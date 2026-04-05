package org.example.canvaslearningassistant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanvasQuizSubmissionQuestionModel {

    private Integer questionId;
    private List<Integer> answersIds;
}