package com.edu.lms.user.repository;

import com.edu.lms.user.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // For OAuth2 lookup — find a user by their Google/GitHub ID
    Optional<User> findByProviderAndProviderId(User.Provider provider, String providerId);


    long countByRole(User.Role role);
    Page<User> findByRole(User.Role role, Pageable pageable);

    long countByInstructorApplicationStatus(User.InstructorApplicationStatus status);
    Page<User> findByRoleAndInstructorApplicationStatus(
        User.Role role, User.InstructorApplicationStatus status, Pageable pageable);
}