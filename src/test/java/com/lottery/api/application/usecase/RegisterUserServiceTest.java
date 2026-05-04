package com.lottery.api.application.usecase;

import com.lottery.api.domain.exception.UserAlreadyExistsException;
import com.lottery.api.domain.model.User;
import com.lottery.api.domain.model.UserRole;
import com.lottery.api.domain.port.out.PasswordEncoderPort;
import com.lottery.api.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterUserService — Tests Unitarios")
class RegisterUserServiceTest {

    @Mock private UserRepositoryPort userRepository;
    @Mock private PasswordEncoderPort passwordEncoder;
    @InjectMocks private RegisterUserService service;

    @Test
    @DisplayName("debe registrar usuario con rol USER y contraseña hasheada")
    void execute_validInput_savesUserWithHashedPassword() {
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("hashed-pass");
        when(userRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        User result = service.execute("jethro", "jethro@test.com", "pass123");

        User saved = captor.getValue();
        assertThat(saved.getId()).isNotBlank();
        assertThat(saved.getUsername()).isEqualTo("jethro");
        assertThat(saved.getEmail()).isEqualTo("jethro@test.com");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed-pass");
        assertThat(saved.getRole()).isEqualTo(UserRole.USER);
        assertThat(saved.isActive()).isTrue();
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("el ID generado debe ser un UUID no vacío")
    void execute_generatesNonBlankId() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = service.execute("user1", "u@test.com", "pass");

        assertThat(result.getId()).isNotBlank();
        assertThat(result.getId()).hasSize(36); // UUID length
    }

    @Test
    @DisplayName("debe lanzar UserAlreadyExistsException si el email ya existe")
    void execute_duplicateEmail_throws() {
        when(userRepository.existsByEmail("dup@test.com")).thenReturn(true);

        assertThatThrownBy(() -> service.execute("newuser", "dup@test.com", "pass"))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("email");
    }

    @Test
    @DisplayName("debe lanzar UserAlreadyExistsException si el username ya existe")
    void execute_duplicateUsername_throws() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThatThrownBy(() -> service.execute("taken", "new@test.com", "pass"))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("username");
    }
}
