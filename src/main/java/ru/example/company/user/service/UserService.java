package ru.example.company.user.service;

import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.example.company.configuration.CustomUserDetails;
import ru.example.company.house.service.HouseService;
import ru.example.company.user.model.User;
import ru.example.company.user.model.UserRole;
import ru.example.company.user.repository.UserRepository;
import ru.example.company.util.validation.ValidationException;
import ru.example.company.util.validation.ValidatorUtil;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final HouseService houseService;
    private final PasswordEncoder passwordEncoder;
    private final ValidatorUtil validatorUtil;

    public Page<User> findAllPages(int page, int size) {
        return userRepository.findAll(PageRequest.of(page - 1, size, Sort.by("id").ascending()));
    }

    public User findByLoginOrEmail(String login) {
        return userRepository.findOneByUsernameOrEmailIgnoreCase(login);
    }

    public User createUser(String login, String email, String password, String passwordConfirm, UUID houseId) {
        return createUser(login, email, password, passwordConfirm, houseId, UserRole.USER);
    }

    public User createUser(String login, String email, String password, String passwordConfirm, UUID houseId, UserRole role) {
        if (userRepository.findOneByUsernameIgnoreCase(login) != null) {
            throw new ValidationException(String.format("User '%s' already exists", login));
        }
        final User user = new User();
        user.setUsername(login);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setHouse(houseService.findById(houseId));
        user.setTwoFactorSecret(TimeBasedOneTimePasswordUtil.generateBase32Secret());
        user.setTwoFactorEnabled(false);
        validatorUtil.validate(user);
        if (!Objects.equals(password, passwordConfirm)) {
            throw new ValidationException("Passwords not equals");
        }
        return userRepository.save(user);
    }
    public User updateUser(User user){
        user.setTwoFactorEnabled(true);
        return userRepository.save(user);
    }
}