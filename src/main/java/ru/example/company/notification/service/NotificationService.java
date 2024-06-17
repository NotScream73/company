package ru.example.company.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.example.company.notification.model.dto.NotificationViewDto;
import ru.example.company.notification.repository.NotificationRepository;
import ru.example.company.user.model.User;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public Integer hasNotifications(User user) {
        return notificationRepository.existsNotificationByUser(user.getId());
    }

    public Page<NotificationViewDto> findAllActualByUserId(UUID userId, Pageable pageable) {
        return notificationRepository.findAllActualByUserId(userId, pageable);
    }
    public void removeNotification(UUID newsId, UUID userId){
        var notification = notificationRepository.findByUserIdAndNewsId(userId, newsId);
        if (notification == null)
            return;
        notification.setIsRemoved(true);
        notificationRepository.save(notification);
    }

    public void readNotification(UUID newsId, UUID userId) {
        var notification = notificationRepository.findByUserIdAndNewsId(userId, newsId);
        if (notification == null)
            return;
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }
}
