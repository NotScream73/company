package ru.example.company.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class UserNewsKey implements Serializable {
    @Column(name = "user_id")
    private UUID userId;
    @Column(name = "news_id")
    private UUID newsId;
}
