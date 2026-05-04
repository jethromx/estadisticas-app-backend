package com.lottery.api.application.usecase;

import com.lottery.api.domain.exception.UserNotFoundException;
import com.lottery.api.domain.port.in.DeleteUserUseCase;
import com.lottery.api.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteUserService implements DeleteUserUseCase {

    private final UserRepositoryPort userRepository;

    @Override
    @Transactional
    public void execute(String id) {
        if (!userRepository.findById(id).isPresent()) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }
}