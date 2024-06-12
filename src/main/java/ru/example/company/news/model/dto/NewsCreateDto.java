package ru.example.company.news.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import ru.example.company.news.model.NewsCategory;
import ru.example.company.user.model.User;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsCreateDto {
    @NotBlank(message = "Title cannot be empty")
    private String title;

    @NotBlank(message = "Title cannot be empty")
    private String description;

    private MultipartFile image;

    private NewsCategory category = NewsCategory.GENERAL;

    private String imageUrl;
    @NotNull
    private UUID houseId;
}