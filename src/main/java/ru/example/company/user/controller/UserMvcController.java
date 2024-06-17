package ru.example.company.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.example.company.configuration.CustomUserDetails;
import ru.example.company.house.service.HouseService;
import ru.example.company.news.model.NewsCategory;
import ru.example.company.user.model.UserRole;
import ru.example.company.user.model.dto.UserDto;
import ru.example.company.user.model.dto.UserNameDto;
import ru.example.company.user.model.dto.UserSignInDto;
import ru.example.company.user.service.UserService;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class UserMvcController {
    private final UserService userService;
    private final HouseService houseService;

    @GetMapping("/users")
    @Secured({UserRole.AsString.MODERATOR})
    public String getUsers(@RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "5") int size,
                           Model model) {
        final Page<UserDto> users = userService.findAllPages(page, size).map(UserDto::new);
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
        model.addAttribute("user", user);
        if (id == null || id == accountUserDetails.getUser().getId()) {
            model.addAttribute("isUser", true);
            return "me";
        } else {
            model.addAttribute("isUser", false);
            return "me/" + id;
        }
    }

    @GetMapping("/me/edit")
    public String getMeEdit(@AuthenticationPrincipal CustomUserDetails accountUserDetails,
                            Model model) {
        var user = new UserSignInDto();
        var houses = houseService.findAll();
        model.addAttribute("user", user);
        model.addAttribute("houses", houses);
        model.addAttribute("isAdmin", accountUserDetails.getUser().getRole() == UserRole.MODERATOR);
        return "edit-me";
    }

    @PostMapping("/me/edit")
    public String editUser(@ModelAttribute("user") @Valid UserSignInDto userSignInDto,
                           @AuthenticationPrincipal CustomUserDetails accountUserDetails,
                           BindingResult bindingResult,
                           Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            model.addAttribute("houses", houseService.findAll());
            model.addAttribute("isAdmin", accountUserDetails.getUser().getRole() == UserRole.MODERATOR);
            return "edit-me";
        }
        var errors = userService.validateUpdateUser(userSignInDto, accountUserDetails.getUser().getRole());
        if (!errors.isEmpty()) {
            model.addAttribute("validationErrors", errors);
            model.addAttribute("houses", houseService.findAll());
            model.addAttribute("isAdmin", accountUserDetails.getUser().getRole() == UserRole.MODERATOR);
            return "edit-me";
        }
        userService.updateUser(accountUserDetails.getUser().getId(), userSignInDto);
        return "redirect:/me";
    }

    @GetMapping("/users/list/{newsId}")
    @ResponseBody
    public List<UserNameDto> findAllByHouse(@PathVariable("newsId") UUID newsId,
                                            @RequestParam(value = "userId") UUID userId) {
        return userService.findAllUsersByNewsId(newsId, userId);
    }
}
