package org.example.canvaslearningassistant.exceptions;

public class QuizGenerationException extends Exception {

    public QuizGenerationException(String message) {
        super(message);
    }

    public QuizGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}