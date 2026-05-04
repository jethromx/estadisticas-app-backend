package com.lottery.api.infrastructure.adapter.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        @NotBlank String username,
        @Email @NotBlank String email,
        String role,
        Boolean active
) {}