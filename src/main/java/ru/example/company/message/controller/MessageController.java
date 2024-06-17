package ru.example.company.message.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.example.company.configuration.CustomUserDetails;
import ru.example.company.message.service.MessageService;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/messages")
public class MessageController {
    private final MessageService messageService;
    @GetMapping
    public String getAllMessages(@AuthenticationPrincipal CustomUserDetails accountUserDetails,
                                 @PageableDefault(page = 0,
                                         size = 2,
                                         sort = "createdAt",
                                         direction = Sort.Direction.DESC) Pageable pageable,
                                 Model model) {
        var messages = messageService.findPaginatedUserMessages(accountUserDetails.getUser().getId(), pageable);
        model.addAttribute("messages", messages);
        model.addAttribute("currentPage", messages.getPageable().getPageNumber());
        return "messages";
    }
    @PostMapping("/send")
    @ResponseBody
    public ResponseEntity<String> sendNews(@RequestParam UUID newsId, @RequestParam UUID senderId, @RequestParam UUID destinationId) {
        messageService.create(newsId, senderId, destinationId);
        return ResponseEntity.ok("Message sent");
    }

}
