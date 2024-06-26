package ru.example.company.news.model;

import lombok.Getter;

@Getter
public enum NewsCategory {
    GENERAL("Обычная"),
    IMPORTANT("Важно"),
    BREAKING("Срочно");
    private final String displayName;

    NewsCategory(String displayName) {
        this.displayName = displayName;
    }

}
