package com.lottery.api.application.usecase;

import com.lottery.api.domain.exception.UserAlreadyExistsException;
import com.lottery.api.domain.model.User;
import com.lottery.api.domain.model.UserRole;
import com.lottery.api.domain.port.in.RegisterUserUseCase;
import com.lottery.api.domain.port.out.PasswordEncoderPort;
import com.lottery.api.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;

    @Override
    @Transactional
    public User execute(String username, String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("email", email);
        }
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("username", username);
        }

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(UserRole.USER)
                .active(true)
                .build();

        return userRepository.save(user);
    }
}
