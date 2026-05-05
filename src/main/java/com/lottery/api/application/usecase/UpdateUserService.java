package com.lottery.api.application.usecase;

import com.lottery.api.domain.exception.UserAlreadyExistsException;
import com.lottery.api.domain.exception.UserNotFoundException;
import com.lottery.api.domain.model.User;
import com.lottery.api.domain.model.UserRole;
import com.lottery.api.domain.port.in.UpdateUserUseCase;
import com.lottery.api.domain.port.out.PasswordEncoderPort;
import com.lottery.api.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateUserService implements UpdateUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;

    @Override
    @Transactional
    public User execute(String id, String username, String email, String role, Boolean active, String password) {
        log.info("Updating user with id: {}, username: {}, email: {}, role: {}, active: {}", id, username, email, role, active);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        log.info("Found existing user: {}", existingUser.getUsername());

        // Check if username is taken by another user
        if (!existingUser.getUsername().equals(username) && userRepository.existsByUsername(username)) {
            log.warn("Username {} already exists", username);
            throw new UserAlreadyExistsException("username", username);
        }

        // Check if email is taken by another user
        if (!existingUser.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            log.warn("Email {} already exists", email);
            throw new UserAlreadyExistsException("email", email);
        }

        UserRole userRole = "ADMIN".equalsIgnoreCase(role) ? UserRole.ADMIN : UserRole.USER;
        boolean activeValue = active != null ? active : existingUser.isActive();

        log.info("Creating updated user with role: {}, active: {}", userRole, activeValue);

        String passwordHash = (password != null && !password.isBlank())
                ? passwordEncoder.encode(password)
                : existingUser.getPasswordHash();

        User updatedUser = User.builder()
                .id(existingUser.getId())
                .username(username)
                .email(email)
                .passwordHash(passwordHash)
                .role(userRole)
                .active(activeValue)
                .createdAt(existingUser.getCreatedAt()) // Preserve original createdAt
                .build();

        userRepository.save(updatedUser);
        
        // Fetch the updated user from DB to ensure all fields are populated correctly
        User savedUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        
        log.info("Successfully updated user: {}", savedUser.getUsername());

        return savedUser;
    }
}