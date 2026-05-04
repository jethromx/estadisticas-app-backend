package com.lottery.api.application.usecase;

import com.lottery.api.domain.exception.InvalidCredentialsException;
import com.lottery.api.domain.model.User;
import com.lottery.api.domain.model.UserRole;
import com.lottery.api.domain.port.out.PasswordEncoderPort;
import com.lottery.api.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUserService — Tests Unitarios")
class LoginUserServiceTest {

    @Mock private UserRepositoryPort userRepository;
    @Mock private PasswordEncoderPort passwordEncoder;
    @InjectMocks private LoginUserService service;

    private User activeUser() {
        return User.builder()
                .id("u-1").username("jethro").email("j@test.com")
                .passwordHash("hashed").role(UserRole.USER).active(true).build();
    }

    @Test
    @DisplayName("debe devolver el usuario con credenciales correctas")
    void execute_validCredentials_returnsUser() {
        when(userRepository.findByEmail("j@test.com")).thenReturn(Optional.of(activeUser()));
        when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);

        User result = service.execute("j@test.com", "pass");

        assertThat(result.getEmail()).isEqualTo("j@test.com");
    }

    @Test
    @DisplayName("debe lanzar InvalidCredentialsException si el email no existe")
    void execute_unknownEmail_throws() {
        when(userRepository.findByEmail("x@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("x@test.com", "pass"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("debe lanzar InvalidCredentialsException si la contraseña no coincide")
    void execute_wrongPassword_throws() {
        when(userRepository.findByEmail("j@test.com")).thenReturn(Optional.of(activeUser()));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> service.execute("j@test.com", "wrong"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("debe lanzar InvalidCredentialsException si la cuenta está inactiva")
    void execute_inactiveUser_throws() {
        User inactive = User.builder()
                .id("u-2").username("disabled").email("d@test.com")
                .passwordHash("hashed").role(UserRole.USER).active(false).build();
        when(userRepository.findByEmail("d@test.com")).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> service.execute("d@test.com", "pass"))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
