package ru.example.company.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.example.company.configuration.CustomUserDetails;
import ru.example.company.notification.service.NotificationService;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    @GetMapping("")
    public String getNotification(@AuthenticationPrincipal CustomUserDetails accountUserDetails,
                                  Model model,
                                  @PageableDefault(page = 0,
                                          size = 2,
                                          sort = "createdAt",
                                          direction = Sort.Direction.DESC) Pageable pageable){
        var notifications = notificationService.findAllActualByUserId(accountUserDetails.getUser().getId(), pageable);
        model.addAttribute("notifications", notifications);
        model.addAttribute("currentPage", notifications.getPageable().getPageNumber());
        return "notifications";
    }

    @PostMapping("/read/{id}")
    public String readNotification(@PathVariable(required = false) UUID id,
                                   @AuthenticationPrincipal CustomUserDetails accountUserDetails){
        notificationService.readNotification(id, accountUserDetails.getUser().getId());
        return "redirect:/notifications";
    }

    @PostMapping("/remove/{id}")
    public String removeNotification(@PathVariable(required = false) UUID id,
                                     @AuthenticationPrincipal CustomUserDetails accountUserDetails){
        notificationService.removeNotification(id, accountUserDetails.getUser().getId());
        return "redirect:/notifications";
    }
}
