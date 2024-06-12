package ru.example.company.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.example.company.user.model.UserNews;
import ru.example.company.user.model.UserNewsKey;

import java.util.UUID;


public interface UserNewsRepository extends JpaRepository<UserNews, UserNewsKey> {
    UserNews findByUserIdAndNewsId(UUID user_id, UUID news_id);

}