package ru.example.company.news.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.example.company.news.model.News;

import java.util.UUID;

public interface NewsRepository extends JpaRepository<News, UUID> {
}