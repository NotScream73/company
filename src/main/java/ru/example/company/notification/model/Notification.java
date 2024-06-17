package ru.example.company.notification.model;

import jakarta.persistence.*;
import lombok.*;
import ru.example.company.news.model.News;
import ru.example.company.user.model.User;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notifications")
public class Notification {
    @EmbeddedId
    private NotificationKey id = new NotificationKey();

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapsId("userId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapsId("newsId")
    private News news;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Column(name = "is_removed", nullable = false)
    private Boolean isRemoved;
}
