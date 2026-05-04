package com.lottery.api.application.usecase;

import com.lottery.api.domain.exception.UserAlreadyExistsException;
import com.lottery.api.domain.model.User;
import com.lottery.api.domain.model.UserRole;
import com.lottery.api.domain.port.in.CreateUserUseCase;
import com.lottery.api.domain.port.out.PasswordEncoderPort;
import com.lottery.api.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateUserService implements CreateUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;

    @Override
    @Transactional
    public User execute(String username, String email, String password, String role) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("email", email);
        }
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("username", username);
        }

        UserRole userRole = "ADMIN".equalsIgnoreCase(role) ? UserRole.ADMIN : UserRole.USER;

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(userRole)
                .active(true)
                .build();

        return userRepository.save(user);
    }
}
