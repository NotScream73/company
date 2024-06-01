package ru.example.company.twofactorauth.totp;

import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;
import org.springframework.util.StringUtils;
import ru.example.company.twofactorauth.TwoFactorAuthenticationCodeVerifier;
import ru.example.company.user.model.User;

import java.security.GeneralSecurityException;

public class TotpAuthenticationCodeVerifier implements TwoFactorAuthenticationCodeVerifier {

    @Override
    public boolean verify(User user, String code) {
        try {
            return TimeBasedOneTimePasswordUtil.validateCurrentNumber(user.getTwoFactorSecret(),
                    StringUtils.hasText(code) ? Integer.parseInt(code) : 0, 10000);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

}