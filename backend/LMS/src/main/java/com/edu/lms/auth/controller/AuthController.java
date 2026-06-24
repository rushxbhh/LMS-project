package com.edu.lms.auth.controller;

import com.edu.lms.auth.dto.AuthResponse;
import com.edu.lms.auth.dto.LoginRequest;
import com.edu.lms.auth.dto.RegisterRequest;
import com.edu.lms.auth.dto.RegistrationRole;
import com.edu.lms.auth.service.GoogleAuthServiceImpl;
import com.edu.lms.auth.service.OAuth2UserService;
import com.edu.lms.common.exception.BusinessException;
import com.edu.lms.common.response.ApiResponse;
import com.edu.lms.common.util.JwtUtil;
import com.edu.lms.user.entity.User;
import com.edu.lms.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.edu.lms.auth.dto.LocalRegisterRequest;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OAuth2UserService oAuth2UserService;
    private final GoogleAuthServiceImpl googleAuthService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ===== Existing OAuth2 endpoints (unchanged) =====

    @PostMapping("/register")
    @Operation(summary = "register as a oath2 user")
    public ResponseEntity<ApiResponse> registerOAuth2User(
            @RequestBody @Valid RegisterRequest request) {
        User user = oAuth2UserService.registerNewOAuth2User(
                request.getEmail(), request.getName(),
                request.getProviderId(), request.getAvatarUrl(),
                request.getRole());
        return ResponseEntity.ok(ApiResponse.success("Registered", buildAuthResponse(user)));
    }


    @Operation(summary = "login or sign up with Google")
    @PostMapping("/google/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithGoogle(
            @RequestParam String code,
            @RequestParam(required = false) RegistrationRole role) {

        return ResponseEntity.ok(ApiResponse.success("Login successful",
                googleAuthService.login(code, role)));
    }

    // ===== NEW: local email/password login =====

    @PostMapping("/login")
    @Operation(summary = "login with credential")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody LoginRequest request) {

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword())
            );
            User user = (User) auth.getPrincipal();
            return ResponseEntity.ok(ApiResponse.success("Login successful", buildAuthResponse(user)));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    // ===== NEW: local email/password registration =====

    @PostMapping("/register/local")
    @Operation(summary = "local registration")
    public ResponseEntity<ApiResponse<AuthResponse>> registerLocal(
            @Valid @RequestBody LocalRegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("An account with this email already exists");
        }

        boolean registeringAsTeacher = request.role() == RegistrationRole.TEACHER;

        User user = userRepository.save(User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(registeringAsTeacher ? User.Role.TEACHER : User.Role.STUDENT)
                .instructorApplicationStatus(registeringAsTeacher
                        ? User.InstructorApplicationStatus.PENDING : null)
                .provider(User.Provider.LOCAL)
                .build());

        return ResponseEntity.ok(ApiResponse.success("Account created", buildAuthResponse(user)));
    }

    // ===== Helper =====

    private AuthResponse buildAuthResponse(User user) {
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
}