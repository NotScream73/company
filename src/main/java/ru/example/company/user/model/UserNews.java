package ru.example.company.user.model;

import jakarta.persistence.*;
import lombok.*;
import ru.example.company.news.model.News;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "user_news")
public class UserNews {
    @EmbeddedId
    private UserNewsKey id = new UserNewsKey();
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapsId("userId")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapsId("newsId")
    private News news;

    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden;

    @Column(name = "is_favorite", nullable = false)
    private Boolean isFavorite;
}
