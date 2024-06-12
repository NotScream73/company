package ru.example.company.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.example.company.configuration.CustomUserDetails;
import ru.example.company.user.model.UserRole;
import ru.example.company.user.model.dto.UserDto;
import ru.example.company.user.service.UserService;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class UserMvcController {
    private final UserService userService;

    @GetMapping("/users")
    @Secured({UserRole.AsString.MODERATOR})
    public String getUsers(@RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "5") int size,
                           Model model) {
        final Page<UserDto> users = userService.findAllPages(page, size)
                .map(UserDto::new);
        model.addAttribute("users", users);
        final int totalPages = users.getTotalPages();
        final List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                .boxed()
                .toList();
        model.addAttribute("pages", pageNumbers);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("users", users);
        return "users";
    }

    @GetMapping(value = {"/me", "/me/{id}"})
    public String getMe(@PathVariable(required = false) UUID id,
                        @AuthenticationPrincipal CustomUserDetails accountUserDetails,
                        Model model) {
        var user = userService.findById(id, accountUserDetails.getUser().getId());
        if (id == null || id == accountUserDetails.getUser().getId()) {
            model.addAttribute("isUser", true);
        }else{
            model.addAttribute("isUser", false);
        }
        model.addAttribute("user", user);
        return "me";
    }
}
