package ru.example.company.news.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.example.company.news.model.News;
import ru.example.company.news.model.dto.CreateNewsDto;
import ru.example.company.news.repository.NewsRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NewsService {
    private final NewsRepository newsRepository;

    public UUID createNews(CreateNewsDto createNewsDto) {
        final News news = null;
        return newsRepository.save(news).getId();
    }
}
