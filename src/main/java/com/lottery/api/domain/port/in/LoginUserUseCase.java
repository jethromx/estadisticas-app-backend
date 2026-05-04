package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.User;

public interface LoginUserUseCase {
    User execute(String email, String rawPassword);
}
