package ru.example.company.news.model.dto;

import org.springframework.beans.factory.annotation.Value;
import ru.example.company.news.model.NewsCategory;

import java.time.LocalDateTime;

public interface NewsDetailsDto {
    String getTitle();

    String getDescription();

    String getImageUrl();

    NewsCategory getCategory();

    Boolean getIsArchived();

    LocalDateTime getCreatedAt();

    String getHouseAddress();

    Boolean getIsFavourite();

    Long getFavouriteCount();
}
