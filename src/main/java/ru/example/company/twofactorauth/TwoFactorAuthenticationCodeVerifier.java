package ru.example.company.twofactorauth;

import ru.example.company.user.model.User;

public interface TwoFactorAuthenticationCodeVerifier {

    boolean verify(User user, String code);

}