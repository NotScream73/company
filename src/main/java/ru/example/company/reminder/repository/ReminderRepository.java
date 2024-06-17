package ru.example.company.reminder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.example.company.reminder.model.Reminder;

import java.time.LocalDateTime;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByReminderTimeBefore(LocalDateTime time);
}