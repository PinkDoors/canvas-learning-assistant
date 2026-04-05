package org.example.canvaslearningassistant.mappers;

import org.example.canvaslearningassistant.dto.GenerateQuizQuestionsItemResponse;
import org.example.canvaslearningassistant.model.NewQuizQuestionData;
import org.example.canvaslearningassistant.model.QuizQuestionType;

public class NewQuizQuestionDataMapper {
    public static NewQuizQuestionData toModel(GenerateQuizQuestionsItemResponse quizQuestionDto) {
        NewQuizQuestionData question = new NewQuizQuestionData();

        question.setQuestion(quizQuestionDto.getQuestion());
        question.setType(fromValue(quizQuestionDto.getType()));
        question.setOptions(quizQuestionDto.getOptions());
        question.setCorrectIndexes(quizQuestionDto.getCorrect_index());
        question.setConcept(quizQuestionDto.getConcept());

        return question;
    }

    private static QuizQuestionType fromValue(String value) {
        for (QuizQuestionType type : QuizQuestionType.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown type: " + value);
    }
}
