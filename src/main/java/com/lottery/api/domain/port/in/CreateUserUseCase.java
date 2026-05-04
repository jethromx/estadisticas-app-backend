package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.User;

public interface CreateUserUseCase {
    User execute(String username, String email, String password, String role);
}
