package ru.example.company.reminder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.example.company.reminder.model.Reminder;
import ru.example.company.reminder.repository.ReminderRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReminderService {
    private ReminderRepository reminderRepository;

    public void setReminder(String username, long delay) {
        Reminder reminder = new Reminder();
        reminder.setUsername(username);
        reminder.setReminderTime(LocalDateTime.now().plusSeconds(delay / 1000));
        reminderRepository.save(reminder);
    }
}
