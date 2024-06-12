package ru.example.company.user.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.example.company.configuration.CustomUserDetails;
import ru.example.company.house.service.HouseService;
import ru.example.company.user.model.User;
import ru.example.company.user.model.dto.UserSignupDto;
import ru.example.company.user.service.UserService;
import ru.example.company.util.validation.ValidationException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;


@Controller
@RequestMapping(UserSignupMvcController.SIGNUP_URL)
@RequiredArgsConstructor
public class UserSignupMvcController {
    public static final String SIGNUP_URL = "/signup";

    private final UserService userService;
    private final HouseService houseService;

    @GetMapping
    public String signup(Model model) {
        model.addAttribute("user", new UserSignupDto());
        model.addAttribute("houses", houseService.findAll());
        return "signup";
    }

    @PostMapping
    public String signup(@ModelAttribute("user") @Valid UserSignupDto userSignupDto,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            model.addAttribute("houses", houseService.findAll());
            return "signup";
        }
        // automatic login after signup
        try {
            final User user = userService.createUser(
                    userSignupDto.getUsername(), userSignupDto.getEmail(), userSignupDto.getPassword(), userSignupDto.getPassword(), userSignupDto.getHouseId());
            CustomUserDetails userDetails = new CustomUserDetails(user);
            Authentication token = UsernamePasswordAuthenticationToken.authenticated(userDetails, null,
                    userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(token);
            return "redirect:/";
        } catch (ValidationException e) {
            model.addAttribute("errors", e.getMessage());
            return "signup";
        }
    }

    @GetMapping("/google")
    public String googleSignUp(Model model) {
        model.addAttribute("user", new UserSignupDto());
        model.addAttribute("houses", houseService.findAll());
        return "signup";
    }

    @Value("${vk.service.token}")
    private String serviceToken;

    @GetMapping("/vk")
    public ResponseEntity<?> vkAuth(@RequestParam String payload) {
        try {
            // Декодирование параметра payload
            String decodedPayload = URLDecoder.decode(payload, StandardCharsets.UTF_8.name());

            // Парсинг JSON из декодированного payload
            ObjectMapper objectMapper = new ObjectMapper();
            AuthRequest authRequest = objectMapper.readValue(decodedPayload, AuthRequest.class);

            // Извлечение необходимых данных
            String token = authRequest.getToken();
            String uuid = authRequest.getUuid();

            // Формирование URL для запроса к API Вконтакте
            String url = "https://api.vk.com/method/auth.exchangeSilentAuthToken?v=5.131&token=" + token + "&access_token=" + serviceToken + "&uuid=" + uuid;

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

            return ResponseEntity.ok(response.getBody());
        } catch (UnsupportedEncodingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error decoding payload");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error processing request");
        }
    }

    // AuthRequest.java
    @Setter
    @Getter
    @NoArgsConstructor
    public static class AuthRequest {
        private String type;
        private int auth;
        private User user;
        private String token;
        private int ttl;
        private String uuid;
        private String hash;
        private boolean loadExternalUsers;

        // Getters and setters

        @Getter
        @Setter
        @NoArgsConstructor
        public static class User {
            private long id;
            @JsonProperty("first_name")
            private String firstName;
            @JsonProperty("last_name")
            private String lastName;
            private String avatar;
            @JsonProperty("avatar_base")
            private String avatarBase;
            private String phone;

            // Getters and setters

            public long getId() {
                return id;
            }

            public void setId(long id) {
                this.id = id;
            }

            public String getFirstName() {
                return firstName;
            }

            public void setFirstName(String firstName) {
                this.firstName = firstName;
            }

            public String getLastName() {
                return lastName;
            }

            public void setLastName(String lastName) {
                this.lastName = lastName;
            }

            public String getAvatar() {
                return avatar;
            }

            public void setAvatar(String avatar) {
                this.avatar = avatar;
            }

            public String getAvatarBase() {
                return avatarBase;
            }

            public void setAvatarBase(String avatarBase) {
                this.avatarBase = avatarBase;
            }

            public String getPhone() {
                return phone;
            }

            public void setPhone(String phone) {
                this.phone = phone;
            }
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getAuth() {
            return auth;
        }

        public void setAuth(int auth) {
            this.auth = auth;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public int getTtl() {
            return ttl;
        }

        public void setTtl(int ttl) {
            this.ttl = ttl;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public boolean isLoadExternalUsers() {
            return loadExternalUsers;
        }

        public void setLoadExternalUsers(boolean loadExternalUsers) {
            this.loadExternalUsers = loadExternalUsers;
        }

        @Getter
        @Setter
        class VkAuthResponse {
            @JsonProperty("access_token")
            private String accessToken;
            @JsonProperty("access_token_id")
            private String accessTokenId;
            @JsonProperty("user_id")
            private Long userId;
            private String phone;
            @JsonProperty("phone_validated")
            private Long phoneValidated;
            @JsonProperty("is_service")
            private Boolean isService;
            private String email;
            private Integer source;
            @JsonProperty("source_description")
            private String sourceDescription;
        }

    }
}