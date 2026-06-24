package com.edu.lms.user.service;

import com.edu.lms.common.exception.ResourceNotFoundException;
import com.edu.lms.user.dto.UpdateProfileRequest;
import com.edu.lms.user.dto.UserDto;
import com.edu.lms.user.entity.User;
import com.edu.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserDto.from(user);
    }

    @Transactional
    public UserDto updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setName(request.getName());
        user.setBio(request.getBio());
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        User saved = userRepository.save(user);
        log.info("Updated profile for user: {}", userId);
        return UserDto.from(saved);
    }

    // ===== ADMIN ONLY =====

    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserDto::from);
    }

    @Transactional
    public UserDto changeUserRole(UUID userId, User.Role newRole) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    user.setRole(newRole);

    if (newRole == User.Role.TEACHER) {
        if (user.getInstructorApplicationStatus() != User.InstructorApplicationStatus.APPROVED) {
            user.setInstructorApplicationStatus(User.InstructorApplicationStatus.APPROVED);
        }
    } else {
        user.setInstructorApplicationStatus(null);
    }

    return UserDto.from(userRepository.save(user));
}

    @Transactional
    public void deactivateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(false);
        userRepository.save(user);
        log.info("Deactivated user: {}", userId);
    }

    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable, User.Role roleFilter) {
        Page<User> users = (roleFilter != null)
                ? userRepository.findByRole(roleFilter, pageable)
                : userRepository.findAll(pageable);
        return users.map(UserDto::from);
    }
}