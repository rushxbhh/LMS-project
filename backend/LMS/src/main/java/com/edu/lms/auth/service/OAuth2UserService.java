package com.edu.lms.auth.service;

import com.edu.lms.auth.dto.RegistrationRole;
import com.edu.lms.user.entity.User;
import com.edu.lms.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class OAuth2UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public User registerNewOAuth2User(String email, String name, String providerId,
                                   String avatarUrl, RegistrationRole role) {
    if (userRepository.existsByEmail(email)) {
        throw new RuntimeException("User already registered with this email");
    }

    boolean registeringAsTeacher = role == RegistrationRole.TEACHER;

    return userRepository.save(User.builder()
            .name(name)
            .email(email)
            .role(registeringAsTeacher ? User.Role.TEACHER : User.Role.STUDENT)
            .instructorApplicationStatus(registeringAsTeacher
                    ? User.InstructorApplicationStatus.PENDING : null)
            .provider(User.Provider.GOOGLE)
            .providerId(providerId)
            .avatarUrl(avatarUrl)
            .build());
        }
    } 