package ru.example.company.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.example.company.user.model.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    @Query(value = "SELECT * FROM users WHERE LOWER(username) LIKE LOWER(CONCAT('%', ?1, '%')) OR LOWER(email) LIKE LOWER(CONCAT('%', ?1, '%')) LIMIT 1", nativeQuery = true)
    User findOneByUsernameOrEmailIgnoreCase(String login);
    User findOneByUsernameIgnoreCase(String login);
}