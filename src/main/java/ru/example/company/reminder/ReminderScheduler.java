package ru.example.company.reminder;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.example.company.news.repository.NewsRepository;
import ru.example.company.reminder.controller.ReminderController;
import ru.example.company.reminder.model.Reminder;
import ru.example.company.reminder.repository.ReminderRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReminderScheduler {
    private final ReminderRepository reminderRepository;
    private final ReminderController reminderController;
    private final NewsRepository newsRepository;

    @Scheduled(fixedRate = 60000)
    public void checkReminders() {
        List<Reminder> reminders = reminderRepository.findByReminderTimeBefore(LocalDateTime.now());
        for (Reminder reminder : reminders) {
            var title = newsRepository.findById(reminder.getNewsId()).orElseThrow(() -> new RuntimeException("Не найдена новость"));
            reminderController.sendNotification(reminder.getUsername(), title.getTitle());
            reminderRepository.delete(reminder);
        }
    }
}