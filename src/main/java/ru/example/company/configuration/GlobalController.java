package ru.example.company.configuration;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalController {

    @ModelAttribute
    public void addAttributes(Model model, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        model.addAttribute("requestURI", requestURI);
    }
}