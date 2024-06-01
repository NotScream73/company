package ru.example.company.user.model.dto;

import lombok.Getter;
import lombok.Setter;
import ru.example.company.user.model.User;
import ru.example.company.user.model.UserRole;

import java.util.UUID;

@Getter
@Setter
public class UserDto {
    private final UUID id;
    private final String username;
    private final String email;
    private final UserRole role;

    public UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole();
    }
}
