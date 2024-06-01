package ru.example.company.news.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
public class CreateNewsDto {
    private UUID id;
}
