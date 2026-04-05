package org.example.canvaslearningassistant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanvasQuizSubmissionAnswerModel {

    private Integer questionId;
    private Integer answersId;
}