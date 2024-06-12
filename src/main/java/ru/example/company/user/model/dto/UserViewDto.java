package ru.example.company.user.model.dto;

import lombok.*;
import ru.example.company.house.model.House;
import ru.example.company.user.model.UserRole;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserViewDto {
    private String username;

    private String email;

    private Boolean twoFactorEnabled;

    private UserRole role;

    private House house;
}
