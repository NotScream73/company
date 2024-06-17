package ru.example.company.news.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.example.company.news.model.News;
import ru.example.company.news.model.dto.NewsDetailsDto;

import java.util.UUID;

@Repository
public interface NewsRepository extends JpaRepository<News, UUID>, JpaSpecificationExecutor<News> {
    @Query("SELECT n.title AS title, n.description AS description, n.imageUrl AS imageUrl, n.category AS category, " +
            "n.isArchived AS isArchived, n.createdAt AS createdAt, h.address AS houseAddress, " +
            "(SELECT COUNT(un) FROM UserNews un WHERE un.news.id = n.id) AS favouriteCount, " +
            "(CASE WHEN EXISTS (SELECT un FROM UserNews un WHERE un.news.id = n.id AND un.user.id = :userId) THEN true ELSE false END) AS isFavourite " +
            "FROM News n " +
            "JOIN n.house h " +
            "WHERE n.id = :newsId")
    NewsDetailsDto findByNewsIdAndUserId(@Param("newsId") UUID newsId, @Param("userId") UUID userId);

    @Query(value = "SELECT n FROM News n " +
            "JOIN UserNews u ON n.id = u.id.newsId " +
            "WHERE u.isFavorite = true and u.id.userId = ?1")
    Page<News> findAllNewsByUserIdAndIsFavorite(UUID user_id, Pageable pageable);

    @Query(value = "SELECT n.house.id from News n " +
            "WHERE n.id = ?1")
    UUID findHouseIdById(UUID id);
}