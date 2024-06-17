package ru.example.company.news.model.dto;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import ru.example.company.news.model.NewsCategory;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class NewsFilterDto {
    private String title;

    private NewsCategory category;

    private Boolean isArchived;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdAtFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdAtTo;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime expiresAt;

    private UUID houseId;
}
