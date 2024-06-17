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
public class UserSignupDto {
    @NotBlank(message = "Имя пользователя не должно быть пустым.")
    @Size(min = 1, max = 255, message = "Длина имени пользователя должна быть в диапазоне от 1 до 255 символов.")
    private String username;
    @NotBlank(message = "Пароль не должен быть пустым.")
    @Size(min = 1, max = 255, message = "Длина пароля должна быть в диапазоне от 1 до 255 символов.")
    private String password;
    @NotBlank(message = "Адрес электронной почты не должен быть пустым.")
    @Size(min = 1, max = 255, message = "Длина адреса электронной почты должна быть в диапазоне от 1 до 255 символов.")
    private String email;
    @NotNull(message = "Необходимо выбрать дом.")
    private UUID houseId;
}
