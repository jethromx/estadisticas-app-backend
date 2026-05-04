package com.lottery.api.infrastructure.adapter.web.dto.response;

public record AuthResponse(
        String token,
        String userId,
        String username,
        String email,
        String role
) {}
