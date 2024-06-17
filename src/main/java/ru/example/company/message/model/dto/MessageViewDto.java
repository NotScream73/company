package ru.example.company.message.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageViewDto {
    private String username;
    private String title;
    private LocalDateTime createdAt;
    private UUID newsId;
}
