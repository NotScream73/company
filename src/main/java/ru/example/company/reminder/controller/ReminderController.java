package ru.example.company.reminder.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.example.company.reminder.model.Reminder;
import ru.example.company.reminder.model.dto.ReminderRequest;
import ru.example.company.reminder.repository.ReminderRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Controller
@RequiredArgsConstructor
public class ReminderController {
    private final ReminderRepository reminderRepository;

    @PostMapping("/setReminder")
    @ResponseBody
    public ResponseEntity<String> setReminder(@RequestBody ReminderRequest request) {
        try {
            LocalDateTime reminderTime = LocalDateTime.now().plus(request.getDelay()/1000, ChronoUnit.SECONDS);

            Reminder reminder = new Reminder();
            reminder.setUsername(request.getUsername()); // Здесь нужно указать имя пользователя (можно получать из сессии или аутентификации)
            reminder.setReminderTime(reminderTime);
            reminder.setNewsId(request.getNewsId());

            reminderRepository.save(reminder);

            return ResponseEntity.ok("Напоминание успешно установлено на ");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при установке напоминания");
        }
    }
    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotification(String username, String title) {
        messagingTemplate.convertAndSendToUser(username, "/topic/notifications", "Пора смотреть\n" + title);
    }
}