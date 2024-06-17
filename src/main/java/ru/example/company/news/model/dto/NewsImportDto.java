package ru.example.company.news.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewsImportDto {
    private String title;
    private String description;
    private String imageUrl;
    private String category;
    private String houseAddress;
    private String expiresAt;
    private String createdAt;
    private Boolean isArchived;
}
