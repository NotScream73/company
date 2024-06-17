package ru.example.company.message.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.example.company.message.model.Message;
import ru.example.company.message.model.dto.MessageViewDto;

import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Query(value = "SELECT new ru.example.company.message.model.dto.MessageViewDto(s.username, n.title, m.createdAt, n.id) from Message m " +
            "JOIN m.news n " +
            "JOIN m.senderUser s " +
            "JOIN m.destinationUser d " +
            "where d.id = ?1 " +
            "order by 3 desc")
    Page<MessageViewDto> findALLByDestinationUserIdOOrderByCreatedAtDesc(UUID destinationUserId, Pageable pageable);
}
