package org.example.canvaslearningassistant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AskRequest {

    private String userPrompt;

    // concept -> mastery level
    private Map<String, Integer> knowledge;
}