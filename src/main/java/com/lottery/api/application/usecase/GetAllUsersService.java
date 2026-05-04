package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.User;
import com.lottery.api.domain.port.in.GetAllUsersUseCase;
import com.lottery.api.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAllUsersService implements GetAllUsersUseCase {

    private final UserRepositoryPort userRepository;

    @Override
    public List<User> execute() {
        return userRepository.findAll();
    }
}
