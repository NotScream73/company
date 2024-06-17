package ru.example.company.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.example.company.configuration.CustomUserDetails;
import ru.example.company.news.model.NewsCategory;
import ru.example.company.news.model.dto.NewsFilterDto;
import ru.example.company.news.service.NewsService;
import ru.example.company.notification.service.NotificationService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class IndexController {
    private IndexSettings newsSettings = new IndexSettings();
    private final NotificationService notificationService;
    private final NewsService newsService;

    @GetMapping
    public String home(@AuthenticationPrincipal CustomUserDetails accountUserDetails,
                       Model model) {
        var user = accountUserDetails.getUser();
        var pageable = PageRequest.of(0, 5, Sort.Direction.DESC, "createdAt");
        model.addAttribute("settings", newsSettings);
        var newsList = newsService.findAll(NewsFilterDto.builder().houseId(user.getHouse().getId()).build(), pageable);
        var breakingNewsList = newsService.findAll(NewsFilterDto.builder().houseId(user.getHouse().getId()).category(NewsCategory.BREAKING).build(), pageable);
        var notificationList = notificationService.findAllActualByUserId(user.getId(), pageable);
        model.addAttribute("newsList", newsList);
        model.addAttribute("breakingNewsList", breakingNewsList);
        model.addAttribute("notificationList", notificationList);
        return "index";
    }

    @PostMapping("/updateSettings")
    public String updateSettings(@RequestParam(required = false, defaultValue = "false") boolean displayAllNews,
                                 @RequestParam(required = false, defaultValue = "false") boolean displayBreakingNews,
                                 @RequestParam(required = false, defaultValue = "false") boolean displayNotifications) {
        newsSettings.setDisplayAllNews(displayAllNews);
        newsSettings.setDisplayBreakingNews(displayBreakingNews);
        newsSettings.setDisplayNotifications(displayNotifications);
        return "redirect:/";
    }
}