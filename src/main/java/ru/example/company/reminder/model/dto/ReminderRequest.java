package ru.example.company.reminder.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReminderRequest {
    private String username;
    private long delay; // задержка в миллисекундах
    private UUID newsId; // задержка в миллисекундах

    // Getters and setters
}