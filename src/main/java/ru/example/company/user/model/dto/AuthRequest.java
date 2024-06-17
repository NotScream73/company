package ru.example.company.user.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class AuthRequest {
    private String type;
    private int auth;
    private AuthRequestUserInfo user;
    private String token;
    private int ttl;
    private String uuid;
    private String hash;
    private boolean loadExternalUsers;
}
