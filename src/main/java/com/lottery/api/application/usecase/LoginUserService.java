package com.lottery.api.application.usecase;

import com.lottery.api.domain.exception.InvalidCredentialsException;
import com.lottery.api.domain.model.User;
import com.lottery.api.domain.port.in.LoginUserUseCase;
import com.lottery.api.domain.port.out.PasswordEncoderPort;
import com.lottery.api.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginUserService implements LoginUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public User execute(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isActive()) {
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }
}
