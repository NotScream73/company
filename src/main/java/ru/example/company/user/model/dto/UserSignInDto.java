package ru.example.company.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class UserSignInDto {
    @NotBlank(message = "Имя пользователя не должно быть пустым.")
    @Size(min = 1, max = 255, message = "Длина имени пользователя должна быть в диапазоне от 1 до 255 символов.")
    private String username;
    @NotBlank(message = "Пароль не должен быть пустым.")
    @Size(min = 1, max = 255, message = "Длина пароля должна быть в диапазоне от 1 до 255 символов.")
    private String password;
    private UUID houseId;
}