package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.User;

public interface UpdateUserUseCase {
    User execute(String id, String username, String email, String role, Boolean active);
}