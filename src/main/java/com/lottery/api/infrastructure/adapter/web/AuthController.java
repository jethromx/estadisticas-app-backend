package com.lottery.api.infrastructure.adapter.web;

import com.lottery.api.domain.model.User;
import com.lottery.api.domain.port.in.LoginUserUseCase;
import com.lottery.api.domain.port.in.RegisterUserUseCase;
import com.lottery.api.infrastructure.adapter.web.dto.request.LoginRequest;
import com.lottery.api.infrastructure.adapter.web.dto.request.RegisterRequest;
import com.lottery.api.infrastructure.adapter.web.dto.response.AuthResponse;
import com.lottery.api.infrastructure.config.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Registro e inicio de sesión de usuarios")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final JwtService jwtService;

    @Operation(summary = "Registrar nuevo usuario")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        User user = registerUserUseCase.execute(request.username(), request.email(), request.password());
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(user, token));
    }

    @Operation(summary = "Iniciar sesión")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        User user = loginUserUseCase.execute(request.email(), request.password());
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(toResponse(user, token));
    }

    private AuthResponse toResponse(User user, String token) {
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
    }
}
