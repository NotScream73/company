package ru.example.company.news.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import ru.example.company.news.model.NewsCategory;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsCreateDto {
    @NotBlank(message = "Заголовок не должен быть пустым")
    private String title;

    @NotBlank(message = "Описание не должно быть пустым")
    private String description;

    private MultipartFile image;

    @NotNull(message = "Выберите тип новости")
    private NewsCategory category;

    private String imageUrl;

    @NotNull(message = "Выберите дом")
    private UUID houseId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime expiresAt;
}