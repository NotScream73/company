package ru.example.company.configuration;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;
import ru.example.company.house.exception.HouseNotFoundException;
import ru.example.company.news.exception.NewsNotFoundException;
import ru.example.company.notification.service.NotificationService;
import ru.example.company.user.exception.UserNotFoundException;
import ru.example.company.util.validation.ValidationException;

@RequiredArgsConstructor
@ControllerAdvice
public class GlobalControllerAdvice {

    private final NotificationService notificationService;

    @ModelAttribute
    public void addAttributes(@AuthenticationPrincipal CustomUserDetails accountUserDetails,
                              HttpServletRequest request,
                              Model model) {
        if (accountUserDetails != null) {
            model.addAttribute("hasNotifications", notificationService.hasNotifications(accountUserDetails.getUser()) != 0);
        } else {
            model.addAttribute("hasNotifications", false);
        }
        model.addAttribute("requestURI", request.getRequestURI());
    }

    @ExceptionHandler({
            NewsNotFoundException.class,
            HouseNotFoundException.class,
            ValidationException.class,
            UserNotFoundException.class,
            IllegalArgumentException.class
    })
    public ModelAndView handleError(HttpServletRequest req, Exception ex) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("path", req.getRequestURL());
        mav.addObject("status", HttpStatus.NOT_FOUND.value());
        mav.addObject("error", HttpStatus.NOT_FOUND.name());
        mav.addObject("message", ex.getMessage());
        mav.addObject("returnUrl", req.getHeader("referer"));
        mav.setViewName("error");
        return mav;
    }
    @ExceptionHandler(RuntimeException.class)
    public ModelAndView handleRuntimeException(HttpServletRequest req, Exception ex) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("path", req.getRequestURL());
        mav.addObject("status", 404);
        mav.addObject("error", ex.getClass().getSimpleName());
        mav.addObject("message", ex.getMessage());
        mav.addObject("returnUrl", req.getHeader("referer"));
        mav.setViewName("error");
        return mav;
    }
}
