package ru.example.company.news.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.example.company.configuration.CustomUserDetails;
import ru.example.company.house.service.HouseService;
import ru.example.company.news.model.News;
import ru.example.company.news.model.NewsCategory;
import ru.example.company.news.model.dto.NewsCreateDto;
import ru.example.company.news.model.dto.NewsFilterDto;
import ru.example.company.news.service.NewsService;
import ru.example.company.user.service.UserService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/news")
public class NewsController {
    private final Path root = Paths.get("uploads");
    private final NewsService newsService;
    private final HouseService houseService;
    private final UserService userService;

    @GetMapping
    public String getAllNews(Model model,
                             @ModelAttribute("filter") NewsFilterDto filter,
                             @PageableDefault(page = 0,
                                     size = 2,
                                     sort = "createdAt",
                                     direction = Sort.Direction.DESC) Pageable pageable) {

        var categories = NewsCategory.values();
        var houses = houseService.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("houses", houses);
        if (!model.containsAttribute("filter")) {
            model.addAttribute("filter", filter);
        }
        Page<News> news = newsService.findAll(filter, pageable);
        model.addAttribute("newsList", news);
        model.addAttribute("currentPage", news.getPageable().getPageNumber());
        return "news";
    }

    @GetMapping("{id}")
    public String getNews(@PathVariable("id") UUID newsId,
                          @AuthenticationPrincipal CustomUserDetails accountUserDetails,
                          Model model) {
        final var news = newsService.find(newsId, accountUserDetails.getUser().getId());
        model.addAttribute("news", news);
        model.addAttribute("id", newsId);
        return "details-news";
    }

    @GetMapping(value = {"/edit", "/edit/{id}"})
    public String editNews(@PathVariable(required = false) UUID id,
                           Model model) {
        if (id == null) {
            model.addAttribute("news", new NewsCreateDto());
        } else {
            model.addAttribute("id", id);
            model.addAttribute("news", newsService.findById(id));
        }
        model.addAttribute("houses", houseService.findAll());
        model.addAttribute("categories", NewsCategory.values());
        return "edit-news";
    }

    @PostMapping(value = {"/", "/{id}"})
    public String saveNews(@PathVariable(required = false) UUID id,
                           @ModelAttribute("news") @Valid NewsCreateDto newsCreateDto,
                           BindingResult bindingResult,
                           Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            model.addAttribute("houses", houseService.findAll());
            model.addAttribute("categories", NewsCategory.values());
            return "edit-news";
        }
        if (id == null) {
            newsService.addNews(newsCreateDto);
        } else {
            newsService.updateNews(id, newsCreateDto);
        }
        return "redirect:/news";
    }

    @PostMapping("/favorite/{newsId}")
    @ResponseBody
    public Map<String, String> changeFavoriteStatus(@AuthenticationPrincipal CustomUserDetails accountUserDetails, @PathVariable UUID newsId) {
        var isFavourite = userService.changeFavoriteStatus(accountUserDetails.getUser(), newsId);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Новость добавлена в избранное");
        response.put("favourite", isFavourite ? "true" : "false");
        return response;
    }

    @PostMapping("/hide/{newsId}")
    @ResponseBody
    public Map<String, String> hideNews(@AuthenticationPrincipal CustomUserDetails accountUserDetails, @PathVariable UUID newsId) {
        userService.hideNews(accountUserDetails.getUser(), newsId);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Новость скрыта");
        return response;
    }


    @GetMapping("/archive/{newsId}")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public String archiveNews(@PathVariable UUID newsId) {
        newsService.archiveNews(newsId);
        return "redirect:/news/" + newsId;
    }

    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource resource = newsService.load(filename);
        if (resource.exists() || resource.isReadable()) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/importants")
    public String getImportantListNews(Model model,
                                       @AuthenticationPrincipal CustomUserDetails accountUserDetails,
                                       @PageableDefault(page = 0,
                                               size = 3,
                                               sort = "createdAt",
                                               direction = Sort.Direction.DESC) Pageable pageable) {
        var filter = new NewsFilterDto();
        filter.setCategory(NewsCategory.IMPORTANT);
        filter.setIsArchived(null);
        filter.setHouseId(accountUserDetails.getUser().getHouse().getId());
        var news = newsService.findAll(filter, pageable);
        model.addAttribute("newsList", news);
        model.addAttribute("currentPage", news.getPageable().getPageNumber());
        return "importants";
    }
}
