package ru.example.company.notification.model.dto;

import ru.example.company.news.model.NewsCategory;

import java.time.LocalDateTime;
import java.util.UUID;

public interface NotificationViewDto {
    String getTitle();

    NewsCategory getCategory();

    LocalDateTime getCreatedAt();

    UUID getNewsId();
    Boolean getIsRead();
}
