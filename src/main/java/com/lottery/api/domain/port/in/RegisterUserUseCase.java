package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.User;

public interface RegisterUserUseCase {
    User execute(String username, String email, String rawPassword);
}
