package ru.example.company.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.example.company.news.model.News;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_news")
public class UserNews {
    @EmbeddedId
    private UserNewsKey id;
    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @MapsId("newsId")
    @JoinColumn(name = "news_id")
    private News news;

    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden;

    @Column(name = "is_favorite", nullable = false)
    private Boolean isFavorite;
}
