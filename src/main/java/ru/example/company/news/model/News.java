package ru.example.company.news.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.UuidGenerator;
import ru.example.company.house.model.House;
import ru.example.company.user.model.User;
import ru.example.company.user.model.UserNews;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "news")
@Accessors(chain = true)
public class News {
    @Id
    @UuidGenerator
    @Column(name = "id")
    private UUID id;

    @Column(name = "title", nullable = false, length = 512)
    private String title;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private NewsCategory category = NewsCategory.GENERAL;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_archived", nullable = false)
    private Boolean isArchived = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "house_id", nullable = false)
    private House house;
    @OneToMany(mappedBy = "news", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserNews> userNewsList = new ArrayList<>();

}
