package ru.example.company.news.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.company.house.service.HouseService;
import ru.example.company.news.exception.NewsNotFoundException;
import ru.example.company.news.model.News;
import ru.example.company.news.model.NewsCategory;
import ru.example.company.news.model.dto.NewsCreateDto;
import ru.example.company.news.model.dto.NewsDetailsDto;
import ru.example.company.news.model.dto.NewsFilterDto;
import ru.example.company.news.model.dto.NewsImportDto;
import ru.example.company.news.repository.NewsRepository;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import ru.example.company.notification.model.Notification;
import ru.example.company.notification.model.NotificationKey;
import ru.example.company.notification.repository.NotificationRepository;
import ru.example.company.user.service.UserService;

@Service
@RequiredArgsConstructor
public class NewsService {
    @Value("${upload.path}")
    private String uploadPath;
    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final NewsRepository newsRepository;
    private final HouseService houseService;

    public Page<News> findAll(NewsFilterDto filter, Pageable pageable) {
        NewsSpecification newsSpecification = new NewsSpecification(filter);
        return newsRepository.findAll(newsSpecification, pageable);
    }

    public Page<News> findAllFavourites(UUID userId, Pageable pageable) {
        return newsRepository.findAllNewsByUserIdAndIsFavorite(userId, pageable);
    }

    public NewsDetailsDto find(UUID newsId, UUID userId) {
        final var newsDetailsDto = newsRepository.findByNewsIdAndUserId(newsId, userId);
        return newsDetailsDto;
    }

    @Transactional(readOnly = true)
    public NewsCreateDto findById(UUID newsId) {
        var news = newsRepository.findById(newsId).orElseThrow(() -> new NewsNotFoundException(newsId));
        return NewsCreateDto.builder()
                .title(news.getTitle())
                .description(news.getDescription())
                .category(news.getCategory())
                .imageUrl(news.getImageUrl())
                .houseId(news.getHouse().getId())
                .expiresAt(news.getExpiresAt())
                .build();
    }

    public List<String> validateCreateNewsDto(NewsCreateDto newsCreateDto) {
        var list = new ArrayList<String>();
        if (newsCreateDto.getTitle().isBlank() || newsCreateDto.getTitle().length() > 512){
            list.add("Длина заголовка должна быть не меньше 1 символа и не больше 512 символов.");
        }
        if (newsCreateDto.getDescription().isBlank() || newsCreateDto.getDescription().length() > 2000){
            list.add("Длина описания должна быть не меньше 1 символа и не больше 512 символов.");
        }
        if (newsCreateDto.getCategory() == null){
            list.add("Необходимо выбрать тип новости.");
        }
        if (newsCreateDto.getCategory() == NewsCategory.BREAKING && (newsCreateDto.getExpiresAt() == null || newsCreateDto.getExpiresAt().isBefore(LocalDateTime.now()))){
            list.add("Время окончания срочной новости не может быть раньше текущей даты.");
        }
        if (newsCreateDto.getHouseId() == null){
            list.add("Необходимо выбрать дом.");
        }
        return list;
    }

    public void addNews(NewsCreateDto newsCreateDto) {
        var house = houseService.findById(newsCreateDto.getHouseId());
        var news = News.builder()
                       .title(newsCreateDto.getTitle())
                       .description(newsCreateDto.getDescription())
                       .category(newsCreateDto.getCategory())
                       .createdAt(LocalDateTime.now())
                       .isArchived(false)
                       .house(house)
                       .expiresAt(newsCreateDto.getExpiresAt())
                       .build();

        saveImage(newsCreateDto, news);
        var savedNews = newsRepository.save(news);

        var users = userService.findByHouseId(newsCreateDto.getHouseId());
        var notifications = new ArrayList<Notification>();
        for (var user : users) {
            notifications.add(Notification.builder()
                    .id(new NotificationKey())
                    .user(user)
                    .news(newsRepository.findById(savedNews.getId()).orElseThrow(() -> new RuntimeException("sdfa")))
                    .isRead(false)
                    .isRemoved(false)
                    .build());
        }
        notificationRepository.saveAll(notifications);
    }

    public void updateNews(UUID id, NewsCreateDto newsCreateDto) {
        var news = newsRepository.findById(id).orElseThrow(() -> new RuntimeException("Пизда"));

        news.setTitle(newsCreateDto.getTitle())
                .setDescription(newsCreateDto.getDescription())
                .setCategory(newsCreateDto.getCategory())
                .setIsArchived(false)
                .setImageUrl(!Objects.equals(newsCreateDto.getImage().getOriginalFilename(), "") ? UUID.randomUUID() + "_" + newsCreateDto.getImage().getOriginalFilename() : news.getImageUrl())
                .setHouse(houseService.findById(newsCreateDto.getHouseId()));
        if (news.getCategory() != NewsCategory.BREAKING) {
            news.setExpiresAt(null);
        } else {
            news.setExpiresAt(newsCreateDto.getExpiresAt());
        }
        saveImage(newsCreateDto, news);
        newsRepository.save(news);
    }

    private void saveImage(NewsCreateDto newsCreateDto, News news) {
        if (!newsCreateDto.getImage().isEmpty()) {
            String imageName = UUID.randomUUID() + newsCreateDto.getImage().getOriginalFilename().replace(' ', '_');
            Path fileNameAndPath = Paths.get(uploadPath, imageName);
            try {
                Files.write(fileNameAndPath, newsCreateDto.getImage().getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            news.setImageUrl(imageName);
        }
    }

    public void archiveNews(UUID newsId) {
        var news = newsRepository.findById(newsId).orElseThrow(() -> new RuntimeException("Нет данных"));
        news.setIsArchived(true);
        newsRepository.save(news);
    }

    public void importNews(NewsImportDto newsImportDto) {
        var house = houseService.findIdByAddress(newsImportDto.getHouseAddress());
        News news = new News();
        news.setTitle(newsImportDto.getTitle());
        news.setDescription(newsImportDto.getDescription());
        news.setImageUrl(newsImportDto.getImageUrl());
        news.setCategory(NewsCategory.valueOf(newsImportDto.getCategory()));
        news.setExpiresAt(LocalDateTime.parse(newsImportDto.getExpiresAt()));
        news.setCreatedAt(LocalDateTime.parse(newsImportDto.getCreatedAt()));
        news.setHouse(house);
        if (newsImportDto.getImageUrl() != null) {

            try {
                // Создание объекта URL
                URL url = new URL(newsImportDto.getImageUrl());

                // Извлечение имени файла из URL
                String fileName = new File(url.getPath()).getName();
                String destinationFilePath = uploadPath + fileName;

                // Скачивание и сохранение файла
                File destinationFile = new File(destinationFilePath);
                FileUtils.copyURLToFile(url, destinationFile);
                System.out.println("Image successfully downloaded and saved to: " + destinationFilePath);
            } catch (IOException e) {
                System.err.println("Failed to download the image from: " + newsImportDto.getImageUrl());
            }
        }
        // Сохранение новости
        newsRepository.save(news);
    }
}
