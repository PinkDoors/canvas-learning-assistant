package org.example.canvaslearningassistant.exception;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private final String errorThymeleafTemplate = "error";

    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Exception ex, Model model, HttpServletResponse response, HttpSession session) {
        Integer courseId = (Integer) session.getAttribute("course_id");
        Integer userId = (Integer) session.getAttribute("user_id");

        if (courseId == null || userId == null) {
            log.error(
                    "Exception occurred. Reason: {}",
                    ex.getMessage(), ex
            );
        } else {
            log.error(
                    "Exception occurred for courseId={} and userId={}. Reason: {}",
                    courseId, userId, ex.getMessage(), ex
            );
        }

        response.setStatus(500);
        model.addAttribute("error", "Internal Server Error");
        return errorThymeleafTemplate;
    }
}