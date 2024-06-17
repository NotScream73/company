package ru.example.company.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.example.company.user.model.User;
import ru.example.company.user.model.dto.UserDto;
import ru.example.company.user.model.dto.UserNameDto;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    @Query(value = "SELECT * FROM users WHERE LOWER(username) LIKE LOWER(?1) OR LOWER(email) LIKE LOWER(?1) LIMIT 1", nativeQuery = true)
    User findOneByUsernameOrEmailIgnoreCase(String login);

    User findOneByUsernameIgnoreCase(String login);

    List<User> findAllByHouseId(UUID houseId);

    @Query(value = "select u.id as id, u.username as username from User u where u.house.id = ?1 and u.id != ?2")
    List<UserNameDto> findAllByHouseIdWithoutUser(UUID houseId, UUID userId);
}