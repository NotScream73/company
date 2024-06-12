package ru.example.company.news.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.example.company.house.service.HouseService;
import ru.example.company.news.model.News;
import ru.example.company.news.model.dto.NewsCreateDto;
import ru.example.company.news.model.dto.NewsDetailsDto;
import ru.example.company.news.model.dto.NewsFilterDto;
import ru.example.company.news.repository.NewsRepository;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class NewsService {
    private final NewsRepository newsRepository;
    private final Path root = Paths.get("uploads");
    private final HouseService houseService;

    public Page<News> findAll(NewsFilterDto filter, Pageable pageable) {
        NewsSpecification newsSpecification = new NewsSpecification(filter);
        return newsRepository.findAll(newsSpecification, pageable).map(this::getNews);
    }

    public NewsDetailsDto find(UUID newsId, UUID userId) {
        final var newsDetailsDto = newsRepository.findByNewsIdAndUserId(newsId, userId);
        return newsDetailsDto;
    }

    public News getNews(News news) {
        return news.setImageUrl("news/" + root.resolve(news.getImageUrl() != null ? news.getImageUrl() : ""));
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(root);
        } catch (IOException ignored) {
        }
    }

    public void save(MultipartFile file, String fileName) {
        try {
            if (fileName == null || fileName.isEmpty())
                return;
            Files.copy(file.getInputStream(), this.root.resolve(fileName));
        } catch (Exception e) {
            if (e instanceof FileAlreadyExistsException) {
                throw new RuntimeException("A file of that name already exists.");
            }

            throw new RuntimeException(e.getMessage());
        }
    }

    public Resource load(String filename) {
        try {
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    public boolean delete(String filename) {
        try {
            Path file = root.resolve(filename);
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    public void deleteAll() {
        FileSystemUtils.deleteRecursively(root.toFile());
    }

    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.root, 1).filter(path -> !path.equals(this.root)).map(this.root::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Could not load the files!");
        }
    }

    public NewsCreateDto findById(UUID newsId) {
        var news = newsRepository.findById(newsId).orElseThrow(() -> new RuntimeException("Неет"));
        return NewsCreateDto.builder()
                .title(news.getTitle())
                .description(news.getDescription())
                .imageUrl(news.getImageUrl())
                .houseId(news.getHouse().getId())
                .category(news.getCategory())
                .build();
    }

    public void addNews(NewsCreateDto newsCreateDto) {
        var news = News.builder()
                .title(newsCreateDto.getTitle())
                .description(newsCreateDto.getDescription())
                .category(newsCreateDto.getCategory())
                .createdAt(LocalDateTime.now())
                .isArchived(false)
                .imageUrl(!Objects.equals(newsCreateDto.getImage().getOriginalFilename(), "") ? UUID.randomUUID() + "_" + newsCreateDto.getImage().getOriginalFilename() : null)
                .house(houseService.findById(newsCreateDto.getHouseId()))
                .build();
        save(newsCreateDto.getImage(), news.getImageUrl());
        newsRepository.save(news);
    }

    public void updateNews(UUID id, NewsCreateDto newsCreateDto) {
        var news = newsRepository.findById(id).orElseThrow(() -> new RuntimeException("Пизда"));

        news.setTitle(newsCreateDto.getTitle())
            .setDescription(newsCreateDto.getDescription())
            .setCategory(newsCreateDto.getCategory())
            .setIsArchived(false)
            .setImageUrl(!Objects.equals(newsCreateDto.getImage().getOriginalFilename(), "") ? UUID.randomUUID() + "_" + newsCreateDto.getImage().getOriginalFilename() : news.getImageUrl())
            .setHouse(houseService.findById(newsCreateDto.getHouseId()));
        if (!Objects.equals(newsCreateDto.getImage().getOriginalFilename(), "")){
            save(newsCreateDto.getImage(), news.getImageUrl());
        }
        newsRepository.save(news);
    }

    public void archiveNews(UUID newsId) {
        var news = newsRepository.findById(newsId).orElseThrow(() -> new RuntimeException("Нет данных"));
        news.setIsArchived(true);
        newsRepository.save(news);
    }
}
