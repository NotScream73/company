package ru.example.company.message.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.example.company.message.model.Message;
import ru.example.company.message.model.dto.MessageViewDto;
import ru.example.company.message.repository.MessageRepository;
import ru.example.company.news.repository.NewsRepository;
import ru.example.company.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final NewsRepository newsRepository;
    private final UserRepository userRepository;

    public Page<MessageViewDto> findPaginatedUserMessages(UUID id, Pageable pageable) {
        return messageRepository.findALLByDestinationUserIdOOrderByCreatedAtDesc(id, pageable);
    }

    @Transactional
    public void create(UUID newsId, UUID senderId, UUID destinationId) {
        var news = newsRepository.findById(newsId).orElseThrow(() -> new RuntimeException("Новость не надйена"));
        var destinationUser = userRepository.findById(destinationId).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        var senderUser = userRepository.findById(senderId).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Message message = new Message();
        message.setNews(news);
        message.setDestinationUser(destinationUser);
        message.setSenderUser(senderUser);
        message.setCreatedAt(LocalDateTime.now());
        messageRepository.save(message);
    }
}
