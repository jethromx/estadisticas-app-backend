package com.lottery.api.infrastructure.adapter.persistence;

import com.lottery.api.domain.model.User;
import com.lottery.api.domain.port.out.UserRepositoryPort;
import com.lottery.api.infrastructure.adapter.persistence.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository jpaRepository;

    @Override
    public User save(User user) {
        return toDomain(jpaRepository.save(toEntity(user)));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(String id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    private UserEntity toEntity(User u) {
        return UserEntity.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .passwordHash(u.getPasswordHash())
                .role(u.getRole())
                .active(u.isActive())
                .build();
    }

    private User toDomain(UserEntity e) {
        return User.builder()
                .id(e.getId())
                .username(e.getUsername())
                .email(e.getEmail())
                .passwordHash(e.getPasswordHash())
                .role(e.getRole())
                .active(e.isActive())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
