package ru.example.company.news.exception;

import java.util.UUID;

public class NewsNotFoundException extends RuntimeException {
    public NewsNotFoundException(UUID id) {
        super(String.format("News with id [%s] is not found", id));
    }
}