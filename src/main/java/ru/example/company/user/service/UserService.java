package ru.example.company.user.service;

import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.example.company.house.service.HouseService;
import ru.example.company.news.repository.NewsRepository;
import ru.example.company.user.model.User;
import ru.example.company.user.model.UserNews;
import ru.example.company.user.model.UserNewsKey;
import ru.example.company.user.model.UserRole;
import ru.example.company.user.model.dto.UserViewDto;
import ru.example.company.user.repository.UserNewsRepository;
import ru.example.company.user.repository.UserRepository;
import ru.example.company.util.validation.ValidationException;
import ru.example.company.util.validation.ValidatorUtil;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final HouseService houseService;
    private final PasswordEncoder passwordEncoder;
    private final ValidatorUtil validatorUtil;
    private final UserNewsRepository userNewsRepository;
    private final NewsRepository newsRepository;

    public Page<User> findAllPages(int page, int size) {
        return userRepository.findAll(PageRequest.of(page - 1, size, Sort.by("id").ascending()));
    }

    public UserViewDto findById(UUID targetId, UUID userId) {
        User user;
        if (targetId == null)
            user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Нет такого пользователя."));
        else
            user = userRepository.findById(targetId).orElseThrow(() -> new RuntimeException("Нет такого пользователя."));
        return UserViewDto.builder()
                          .username(user.getUsername())
                          .twoFactorEnabled(user.getTwoFactorEnabled())
                          .email(user.getEmail())
                          .role(user.getRole())
                          .house(user.getHouse())
                          .role(user.getRole())
                          .build();
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

    public User updateUser(User user) {
        user.setTwoFactorEnabled(true);
        return userRepository.save(user);
    }

    public boolean changeFavoriteStatus(User user, UUID newsId) {
        UserNews userNews = userNewsRepository.findByUserIdAndNewsId(user.getId(), newsId);
        if (userNews == null) {
            userNews = UserNews.builder()
                    .id(new UserNewsKey())
                    .user(user)
                    .news(newsRepository.findById(newsId).orElseThrow(() -> new RuntimeException("sdfa")))
                    .isFavorite(true)
                    .isHidden(false)
                    .build();
        } else {
            userNews.setIsFavorite(!userNews.getIsFavorite());
        }
        userNewsRepository.save(userNews);
        return userNews.getIsFavorite();
    }

    public void hideNews(User user, UUID newsId) {
        UserNews userNews = userNewsRepository.findByUserIdAndNewsId(user.getId(), newsId);
        if (userNews == null) {
            userNews = UserNews.builder()
                    .id(new UserNewsKey())
                    .user(user)
                    .news(newsRepository.findById(newsId).orElseThrow(() -> new RuntimeException("sdfa")))
                    .isFavorite(false)
                    .isHidden(true)
                    .build();
        } else {
            userNews.setIsHidden(true);
        }
        userNewsRepository.save(userNews);
    }

    public void disable2FA(UUID id) {
        var user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Нет такого пользователя"));
        user.setTwoFactorEnabled(false);
        userRepository.save(user);
    }
}