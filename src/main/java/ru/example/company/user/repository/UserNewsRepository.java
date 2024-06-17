package ru.example.company.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.example.company.user.model.UserNews;
import ru.example.company.user.model.UserNewsKey;

import java.util.UUID;


@Repository
public interface UserNewsRepository extends JpaRepository<UserNews, UserNewsKey> {
    UserNews findByUserIdAndNewsId(UUID userId, UUID newsId);
}