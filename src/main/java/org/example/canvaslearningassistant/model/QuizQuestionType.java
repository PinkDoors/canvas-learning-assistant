package org.example.canvaslearningassistant.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum QuizQuestionType {

    MULTIPLE_CHOICE("multiple_choice_question"),
    TRUE_FALSE("true_false_question"),
    MULTIPLE_ANSWERS("multiple_answers_question");

    private final String value;

    QuizQuestionType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}