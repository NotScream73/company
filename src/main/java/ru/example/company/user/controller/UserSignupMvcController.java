package ru.example.company.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.example.company.house.service.HouseService;
import ru.example.company.user.model.dto.UserSignupDto;
import ru.example.company.user.service.UserService;

@Controller
@RequestMapping(UserSignupMvcController.SIGNUP_URL)
@RequiredArgsConstructor
public class UserSignupMvcController {
    public static final String SIGNUP_URL = "/signup";

    private final UserService userService;
    private final HouseService houseService;

    @GetMapping
    public String signup(Model model) {
        model.addAttribute("user", new UserSignupDto());
        model.addAttribute("houses", houseService.findAll());
        return "signup";
    }

    @PostMapping
    public String signup(@ModelAttribute("user") @Valid UserSignupDto userSignupDto,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            model.addAttribute("houses", houseService.findAll());
            return "signup";
        }

        var errors = userService.validateUser(userSignupDto);
        if (!errors.isEmpty()) {
            model.addAttribute("validationErrors", errors);
            model.addAttribute("houses", houseService.findAll());
            return "signup";
        }

        userService.signUp(userSignupDto);
        return "redirect:/";
    }

    @GetMapping("/vk")
    public String vkAuth(@RequestParam String payload) {
        return userService.vkAuthorization(payload);
    }
}