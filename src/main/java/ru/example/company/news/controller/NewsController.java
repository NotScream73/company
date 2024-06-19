package ru.example.company.news.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.example.company.configuration.CustomUserDetails;
import ru.example.company.house.service.HouseService;
import ru.example.company.news.model.News;
import ru.example.company.news.model.NewsCategory;
import ru.example.company.news.model.dto.NewsCreateDto;
import ru.example.company.news.model.dto.NewsFilterDto;
import ru.example.company.news.model.dto.NewsImportDto;
import ru.example.company.news.service.NewsService;
import ru.example.company.user.repository.UserNewsRepository;
import ru.example.company.user.service.UserService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/news")
public class NewsController {
    private final NewsService newsService;
    private final HouseService houseService;
    private final UserService userService;
    private final UserNewsRepository userNewsRepository;
    @Value("${upload.path}")
    private String uploadPath;
    private final String prefix = "http://localhost";

    @GetMapping
    public String getAllNews(Model model,
                             @ModelAttribute("filter") NewsFilterDto filter,
                             @AuthenticationPrincipal CustomUserDetails accountUserDetails,
                             @PageableDefault(page = 0,
                                     size = 2,
                                     sort = "createdAt",
                                     direction = Sort.Direction.DESC) Pageable pageable) {
        var user = accountUserDetails.getUser();
        filter.setUserId(user.getId());
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
        model.addAttribute("userId", user.getId());
        model.addAttribute("favourites", userNewsRepository.findNewsIdByUserIdAndIsFavoriteIsNot(user.getId()));
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
    public String editNewsPage(@PathVariable(required = false) UUID id,
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
        var errors = newsService.validateCreateNewsDto(newsCreateDto);
        if (!errors.isEmpty()) {
            model.addAttribute("validationErrors", errors);
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
    public String hideNews(@AuthenticationPrincipal CustomUserDetails accountUserDetails, HttpServletRequest req, @PathVariable UUID newsId) {
        userService.hideNews(accountUserDetails.getUser(), newsId);
        var url = req.getHeader("referer");
        return "redirect:" + url.substring(prefix.length());
    }


    @GetMapping("/archive/{newsId}")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public String archiveNews(@PathVariable UUID newsId) {
        newsService.archiveNews(newsId);
        return "redirect:/news/" + newsId;
    }

    @GetMapping("/breakings")
    public String getBreakingsListNews(Model model,
                                       @AuthenticationPrincipal CustomUserDetails accountUserDetails,
                                       @PageableDefault(page = 0,
                                               size = 3,
                                               sort = "createdAt",
                                               direction = Sort.Direction.DESC) Pageable pageable) {
        var filter = new NewsFilterDto();
        var user = accountUserDetails.getUser();
        filter.setCategory(NewsCategory.BREAKING);
        filter.setIsArchived(null);
        filter.setHouseId(user.getHouse().getId());
        filter.setExpiresAt(LocalDateTime.now());
        var news = newsService.findAll(filter, pageable);
        model.addAttribute("newsList", news);
        model.addAttribute("currentPage", news.getPageable().getPageNumber());
        model.addAttribute("userId", user.getId());
        model.addAttribute("favourites", userNewsRepository.findNewsIdByUserIdAndIsFavoriteIsNot(user.getId()));
        return "breakings";
    }

    @GetMapping("/favourites")
    public String getFavouritesListNews(Model model,
                                        @AuthenticationPrincipal CustomUserDetails accountUserDetails,
                                        @PageableDefault(page = 0,
                                                size = 3,
                                                sort = "createdAt",
                                                direction = Sort.Direction.DESC) Pageable pageable) {
        var news = newsService.findAllFavourites(accountUserDetails.getUser().getId(), pageable);
        model.addAttribute("newsList", news);
        model.addAttribute("currentPage", news.getPageable().getPageNumber());
        model.addAttribute("userId", accountUserDetails.getUser().getId());
        model.addAttribute("favourites", userNewsRepository.findNewsIdByUserIdAndIsFavoriteIsNot(accountUserDetails.getUser().getId()));
        return "favourites";
    }

    @GetMapping("/import")
    public String showImportForm() {
        return "import";
    }

    @PostMapping("/import")
    public String importNews(@RequestParam("file") MultipartFile file) {
        try {
            // Чтение содержимого файла
            byte[] bytes;
            try {
                bytes = file.getBytes();
            } catch (Exception exception) {
                return null;
            }
            String jsonString = new String(bytes, StandardCharsets.UTF_8);

            // Десериализация JSON в объект DTO
            ObjectMapper objectMapper = new ObjectMapper();
            NewsImportDto newsImportDto = objectMapper.readValue(jsonString, NewsImportDto.class);

            // Преобразование DTO в вашу сущность News и сохранение в базу данных
            newsService.importNews(newsImportDto);

            return "redirect:/news"; // Перенаправление на страницу со списком новостей
        } catch (IOException e) {
            e.printStackTrace(); // Обработка ошибок при чтении или десериализации файла
            return "import";
        }
    }

    @GetMapping("/images/{url}")
    @ResponseBody
    public ResponseEntity<byte[]> getImage(@PathVariable("url") String url) {
        // Путь к изображению
        String imagePath = uploadPath + url; // Укажите путь к вашему изображению
        // Чтение файла из системы
        File file = new File(imagePath);

        // Проверяем, существует ли файл
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        // Чтение содержимого файла в байтовый массив
        Path path = Paths.get(imagePath);
        byte[] imageBytes;
        try {

            imageBytes = Files.readAllBytes(path);
        } catch (Exception exception) {
            return null;
        }

        // Определяем MediaType изображения
        MediaType mediaType = MediaType.IMAGE_JPEG; // Измените в зависимости от типа изображения

        // Возвращаем ResponseEntity с содержимым файла и заголовками
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(imageBytes);
    }
}
