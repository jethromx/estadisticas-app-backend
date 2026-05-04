package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.User;

import java.util.List;

public interface GetAllUsersUseCase {
    List<User> execute();
}
