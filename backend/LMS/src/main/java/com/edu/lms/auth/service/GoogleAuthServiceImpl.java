package com.edu.lms.auth.service;

import com.edu.lms.auth.dto.AuthResponse;
import com.edu.lms.auth.dto.RegistrationRole;
import com.edu.lms.common.util.JwtUtil;
import com.edu.lms.user.entity.User;
import com.edu.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleAuthServiceImpl {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    private static final String TOKEN_URL   = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

  public AuthResponse login(String code, RegistrationRole intendedRole) {
    String accessToken = exchangeCodeForToken(code);
    Map<String, Object> userInfo = fetchUserInfo(accessToken);

    String email      = (String) userInfo.get("email");
    String name       = (String) userInfo.get("name");
    String providerId = (String) userInfo.get("sub");
    String avatarUrl  = (String) userInfo.get("picture");

    // intendedRole only ever applies to account CREATION below.
    // An existing user's role is never touched here — login isn't registration.
    User user = userRepository.findByEmail(email)
            .orElseGet(() -> {
                boolean registeringAsTeacher = intendedRole == RegistrationRole.TEACHER;
                return userRepository.save(User.builder()
                        .email(email)
                        .name(name)
                        .providerId(providerId)
                        .avatarUrl(avatarUrl)
                        .role(registeringAsTeacher ? User.Role.TEACHER : User.Role.STUDENT)
                        .instructorApplicationStatus(registeringAsTeacher
                                ? User.InstructorApplicationStatus.PENDING : null)
                        .provider(User.Provider.GOOGLE)
                        .build());
            });

    return AuthResponse.builder()
        .token(jwtUtil.generateAccessToken(user))
        .refreshToken(jwtUtil.generateRefreshToken(user))
        .userId(user.getId().toString())
        .email(user.getEmail())
        .name(user.getName())
        .role(user.getRole().name())
        .avatarUrl(user.getAvatarUrl())
        .instructorApplicationStatus(user.getInstructorApplicationStatus() != null
                ? user.getInstructorApplicationStatus().name() : null)
        .build();
}

    // ── private helpers ──────────────────────────────────────────────────────

    private String exchangeCodeForToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code",          code);
        body.add("client_id",     clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri",  redirectUri);
        body.add("grant_type",    "authorization_code");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                TOKEN_URL, new HttpEntity<>(body, headers), Map.class);

        if (response.getBody() == null || !response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Google token exchange failed");
        }
        return (String) response.getBody().get("access_token");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<Map> response = restTemplate.exchange(
                USER_INFO_URL, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        if (response.getBody() == null || !response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Google user-info fetch failed");
        }
        return response.getBody();
    }
}