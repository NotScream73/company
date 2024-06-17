package ru.example.company.message.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.UuidGenerator;
import ru.example.company.news.model.News;
import ru.example.company.user.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "messages")
@Accessors(chain = true)
public class Message {
    @Id
    @UuidGenerator
    @Column(name = "id")
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "destination_user_id", nullable = false)
    private User destinationUser;
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "sender_user_id", nullable = false)
    private User senderUser;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "news_id", nullable = false)
    private News news;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
