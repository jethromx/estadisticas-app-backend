package com.lottery.api.domain.port.out;

import com.lottery.api.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findByEmail(String email);
    Optional<User> findById(String id);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    List<User> findAll();
}
