package ru.example.company.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import ru.example.company.configuration.CustomUserDetails;
import ru.example.company.house.service.HouseService;
import ru.example.company.news.exception.NewsNotFoundException;
import ru.example.company.news.model.NewsCategory;
import ru.example.company.news.repository.NewsRepository;
import ru.example.company.user.controller.UserSignupMvcController;
import ru.example.company.user.exception.UserNotFoundException;
import ru.example.company.user.model.User;
import ru.example.company.user.model.UserNews;
import ru.example.company.user.model.UserNewsKey;
import ru.example.company.user.model.UserRole;
import ru.example.company.user.model.dto.*;
import ru.example.company.user.repository.UserNewsRepository;
import ru.example.company.user.repository.UserRepository;
import ru.example.company.util.validation.ValidationException;
import ru.example.company.util.validation.ValidatorUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final HouseService houseService;
    private final PasswordEncoder passwordEncoder;
    private final ValidatorUtil validatorUtil;
    private final UserNewsRepository userNewsRepository;
    private final NewsRepository newsRepository;

    @Value("${vk.service.token}")
    private String serviceToken;

    @Transactional(readOnly = true)
    public Page<User> findAllPages(int page, int size) {
        return userRepository.findAll(PageRequest.of(page - 1, size, Sort.by("id").ascending()));
    }

    private final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$";

    public boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public List<String> validateUser(UserSignupDto userSignupDto) {
        var list = new ArrayList<String>();
        if (userSignupDto.getUsername().isBlank() || userSignupDto.getUsername().length() > 255) {
            list.add("Длина имени пользователя должна быть не меньше 1 символа и не больше 255 символов.");
        }
        if (userSignupDto.getEmail().isBlank() || userSignupDto.getEmail().length() > 255) {
            list.add("Длина адреса электронной почты должна быть не меньше 1 символа и не больше 255 символов.");
        }
        if (!isValidEmail(userSignupDto.getEmail())) {
            list.add("Адрес электронной почты некорректный.");
        }
        if (userSignupDto.getPassword().isBlank() || userSignupDto.getPassword().length() > 255) {
            list.add("Длина пароля должна быть не меньше 1 символа и не больше 255 символов.");
        }
        if (userSignupDto.getHouseId() == null) {
            list.add("Необходимо выбрать дом.");
        }
        return list;
    }

    @Transactional(readOnly = true)
    public UserViewDto findById(UUID targetId, UUID userId) {
        User user;
        if (targetId == null)
            user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Нет такого пользователя."));
        else
            user = userRepository.findById(targetId).orElseThrow(() -> new RuntimeException("Нет такого пользователя."));
        return UserViewDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .twoFactorEnabled(user.getTwoFactorEnabled())
                .email(user.getEmail())
                .role(user.getRole())
                .house(user.getHouse())
                .role(user.getRole())
                .build();
    }

    @Transactional(readOnly = true)
    public User findByLoginOrEmail(String login) {
        return userRepository.findOneByUsernameOrEmailIgnoreCase(login);
    }

    @Transactional
    public User createUser(String login, String email, String password, UUID houseId) {
        return createUser(login, email, password, houseId, UserRole.USER);
    }

    @Transactional
    public User createUser(String login, String email, String password, UUID houseId, UserRole role) {
        if (userRepository.findOneByUsernameOrEmailIgnoreCase(login) != null || userRepository.findOneByUsernameOrEmailIgnoreCase(email) != null) {
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
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(User user) {
        user.setTwoFactorEnabled(true);
        return userRepository.save(user);
    }

    @Transactional
    public void updateUser(UUID id, UserSignInDto dto) {
        var user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        if (userRepository.findOneByUsernameOrEmailIgnoreCase(dto.getUsername()) != null) {
            throw new ValidationException(String.format("User '%s' already exists", dto.getUsername()));
        }
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);
    }

    @Transactional
    public boolean changeFavoriteStatus(User user, UUID newsId) {
        UserNews userNews = userNewsRepository.findByUserIdAndNewsId(user.getId(), newsId);
        if (userNews == null) {
            userNews = UserNews.builder()
                    .id(new UserNewsKey())
                    .user(user)
                    .news(newsRepository.findById(newsId).orElseThrow(() -> new NewsNotFoundException(newsId)))
                    .isFavorite(true)
                    .isHidden(false)
                    .build();
        } else {
            userNews.setIsFavorite(!userNews.getIsFavorite());
        }
        userNewsRepository.save(userNews);
        return userNews.getIsFavorite();
    }

    @Transactional
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

    @Transactional
    public void disable2FA(UUID id) {
        var user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Нет такого пользователя"));
        user.setTwoFactorEnabled(false);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> findByHouseId(UUID houseId) {
        return userRepository.findAllByHouseId(houseId);
    }

    @Transactional
    public void setVkIdByEmail(String email, long vkId) {
        var user = userRepository.findOneByUsernameOrEmailIgnoreCase(email);
        if (user != null && user.getVkId() == null) {
            user.setVkId(vkId);
            userRepository.save(user);
        }
    }

    @Transactional(readOnly = true)
    public List<UserNameDto> findAllUsersByNewsId(UUID newsId, UUID userId) {
        var houseId = newsRepository.findHouseIdById(newsId);
        return userRepository.findAllByHouseIdWithoutUser(houseId, userId);
    }

    @Transactional(readOnly = true)
    public List<UserNameDto> findAllUsers(User user) {
        return userRepository.findAllByHouseIdWithoutUser(user.getHouse().getId(), user.getId());
    }

    @Transactional
    public User createUser(UserSignupDto userSignupDto) {
        return createUser(userSignupDto.getUsername(), userSignupDto.getEmail(), userSignupDto.getPassword(), userSignupDto.getHouseId());
    }

    @Transactional
    public void signUp(UserSignupDto userSignupDto) {
        var user = createUser(userSignupDto);
        if (user == null) {
            throw new RuntimeException("Не удалось зарегистрироваться");
        }
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication token = UsernamePasswordAuthenticationToken.authenticated(userDetails, null,
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Transactional
    public String vkAuthorization(String payload) {
        try {
            String decodedPayload = URLDecoder.decode(payload, StandardCharsets.UTF_8);

            ObjectMapper objectMapper = new ObjectMapper();
            AuthRequest authRequest = objectMapper.readValue(decodedPayload, AuthRequest.class);

            String token = authRequest.getToken();
            String uuid = authRequest.getUuid();

            String url = "https://api.vk.com/method/auth.exchangeSilentAuthToken?v=5.131&token=" + token + "&access_token=" + serviceToken + "&uuid=" + uuid;

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

            String username = "";
            objectMapper = new ObjectMapper();

            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode responseNode = rootNode.path("response");

            String email = responseNode.path("email").asText();
            long userId = responseNode.path("user_id").asLong();
            String accessToken = responseNode.path("access_token").asText();

            String accessUrl = "https://api.vk.com/method/users.get?v=5.131&fields=screen_name&access_token=" + accessToken;

            ResponseEntity<String> accessResponse = restTemplate.postForEntity(accessUrl, null, String.class);
            objectMapper = new ObjectMapper();

            rootNode = objectMapper.readTree(accessResponse.getBody());
            responseNode = rootNode.path("response");

            if (responseNode.isArray() && !responseNode.isEmpty()) {
                JsonNode firstObject = responseNode.get(0);
                username = firstObject.path("screen_name").asText();
            }

            var user = findByLoginOrEmail(email);
            if (user == null && username != null) {
                var password = UUID.randomUUID().toString();
                user = createUser(username, email, password, houseService.findAll().getFirst().getId(), UserRole.USER);
                user.setVkId(userId);
            } else {
                setVkIdByEmail(email, userId);
            }

            CustomUserDetails userDetails = new CustomUserDetails(user);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            return "redirect:/";
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при авторизации через ВК");
        }
    }

    public List<String> validateUpdateUser(UserSignInDto userSignInDto, UserRole role) {
        var list = new ArrayList<String>();
        if (userSignInDto.getUsername().isBlank() || userSignInDto.getUsername().length() > 512) {
            list.add("Длина имени пользователя должна быть не меньше 1 символа и не больше 255 символов.");
        }
        if (userSignInDto.getPassword().isBlank() || userSignInDto.getPassword().length() > 2000) {
            list.add("Длина пароля должна быть не меньше 1 символа и не больше 255 символов.");
        }
        if (role == UserRole.MODERATOR && userSignInDto.getHouseId() == null) {
            list.add("Необходимо выбрать дом.");
        }
        return list;
    }
}