package ru.example.company.notification.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.example.company.notification.model.Notification;
import ru.example.company.notification.model.NotificationKey;
import ru.example.company.notification.model.dto.NotificationViewDto;

import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, NotificationKey> {
    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM notifications where user_id = ?1 and is_read = false and is_removed = false ")
    Integer existsNotificationByUser(UUID userId);

    @Query("SELECT n.news.title as title, n.news.category as category, n.news.createdAt as createdAt, n.id.newsId as newsId, n.isRead as isRead " +
            "FROM Notification n " +
            "WHERE n.id.userId = :userId and n.isRemoved = false")
    Page<NotificationViewDto> findAllActualByUserId(@Param("userId") UUID userId, Pageable pageable);
    Notification findByUserIdAndNewsId(UUID userId, UUID newsId);
}
