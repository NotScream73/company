package ru.example.company.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserSignInDto {
    @NotBlank
    @Size(min = 3, max = 64)
    private String username;
    @NotBlank
    @Size(min = 6, max = 64)
    private String password;
}