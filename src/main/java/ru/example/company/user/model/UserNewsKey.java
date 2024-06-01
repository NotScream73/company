package ru.example.company.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Embeddable
public class UserNewsKey implements Serializable {
    @Column(name = "user_id")
    private UUID userId;
    @Column(name = "news_id")
    private UUID newsId;
}
