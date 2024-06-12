package ru.example.company.user.controller;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.example.company.configuration.CustomUserDetails;
import ru.example.company.twofactorauth.TwoFactorAuthentication;
import ru.example.company.twofactorauth.TwoFactorAuthenticationCodeVerifier;
import ru.example.company.twofactorauth.totp.QrCode;
import ru.example.company.user.model.User;
import ru.example.company.user.service.UserService;

@Controller
@RequiredArgsConstructor
public class TwoFactorAuthController {

    private final UserService userService;

    private final TwoFactorAuthenticationCodeVerifier codeVerifier;

    private final QrCode qrCode;

    private final AuthenticationSuccessHandler successHandler;

    private final AuthenticationFailureHandler failureHandler;

    @GetMapping(path = "/enable-2fa")
    public String requestEnableTwoFactor(@AuthenticationPrincipal CustomUserDetails accountUserDetails, Model model) {
        User user = accountUserDetails.getUser();
        String otpAuthUrl = "otpauth://totp/%s?secret=%s&digits=6".formatted("Demo: " + user.getUsername(),
                user.getTwoFactorSecret());
        model.addAttribute("qrCode", this.qrCode.dataUrl(otpAuthUrl));
        model.addAttribute("secret", user.getTwoFactorSecret());
        return "enable-2fa";
    }

    @PostMapping(path = "/enable-2fa")
    public String processEnableTwoFactor(@RequestParam String code,
                                         @AuthenticationPrincipal CustomUserDetails accountUserDetails, Model model) {
        User user = accountUserDetails.getUser();
        if (user.getTwoFactorEnabled()) {
            return "redirect:/";
        }
        if (!this.codeVerifier.verify(user, code)) {
            model.addAttribute("message", "Invalid code");
            return this.requestEnableTwoFactor(accountUserDetails, model);
        }
        User enabled = userService.updateUser(user);
        Authentication token = UsernamePasswordAuthenticationToken.authenticated(new CustomUserDetails(enabled), null,
                accountUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);
        return "redirect:/";
    }

    @GetMapping(path = "/disable-2fa")
    public String processDisableTwoFactor(@AuthenticationPrincipal CustomUserDetails accountUserDetails, Model model) {
        userService.disable2FA(accountUserDetails.getUser().getId());
        return "redirect:/me";
    }

    @GetMapping(path = "/challenge/totp")
    public String requestTotp() {
        return "totp";
    }

    @PostMapping(path = "/challenge/totp")
    public void processTotp(@RequestParam String code,
                            HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Authentication primaryAuthentication = ((TwoFactorAuthentication) SecurityContextHolder.getContext().getAuthentication()).getPrimary();
        CustomUserDetails accountUserDetails = (CustomUserDetails) primaryAuthentication.getPrincipal();
        User user = accountUserDetails.getUser();
        if (this.codeVerifier.verify(user, code)) {
            SecurityContextHolder.getContext().setAuthentication(primaryAuthentication);
            this.successHandler.onAuthenticationSuccess(request, response, primaryAuthentication);
        }
        else {
            this.failureHandler.onAuthenticationFailure(request, response, new BadCredentialsException("Invalid code"));
        }
    }

}
