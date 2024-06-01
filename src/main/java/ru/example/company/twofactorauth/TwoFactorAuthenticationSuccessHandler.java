package ru.example.company.twofactorauth;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import ru.example.company.configuration.CustomUserDetails;
import ru.example.company.user.model.User;

public class TwoFactorAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthenticationSuccessHandler primarySuccessHandler;

    private final AuthenticationSuccessHandler secondarySuccessHandler;

    public TwoFactorAuthenticationSuccessHandler(String secondAuthUrl,
                                                 AuthenticationSuccessHandler primarySuccessHandler) {
        this.primarySuccessHandler = primarySuccessHandler;
        this.secondarySuccessHandler = new SimpleUrlAuthenticationSuccessHandler(secondAuthUrl);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = customUserDetails.getUser();
        if (user.getTwoFactorEnabled()) {
            SecurityContextHolder.getContext().setAuthentication(new TwoFactorAuthentication(authentication));
            this.secondarySuccessHandler.onAuthenticationSuccess(request, response, authentication);
        }
        else {
            this.primarySuccessHandler.onAuthenticationSuccess(request, response, authentication);
        }
    }

}