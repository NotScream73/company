package ru.example.company.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.example.company.configuration.CustomUserDetails;
import ru.example.company.house.service.HouseService;
import ru.example.company.user.model.User;
import ru.example.company.user.model.dto.UserSignupDto;
import ru.example.company.user.service.UserService;
import ru.example.company.util.validation.ValidationException;


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
        // automatic login after signup
        try {
            final User user = userService.createUser(
                    userSignupDto.getUsername(),userSignupDto.getEmail(), userSignupDto.getPassword(), userSignupDto.getPassword(), userSignupDto.getHouseId());
            CustomUserDetails userDetails = new CustomUserDetails(user);
            Authentication token = UsernamePasswordAuthenticationToken.authenticated(userDetails, null,
                    userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(token);
            return "redirect:/";
        } catch (ValidationException e) {
            model.addAttribute("errors", e.getMessage());
            return "signup";
        }

    }
}
