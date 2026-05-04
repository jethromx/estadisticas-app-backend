package com.lottery.api.infrastructure.adapter.web.dto.response;

import java.time.LocalDateTime;

public record AdminUserResponse(
        String id,
        String username,
        String email,
        String role,
        boolean active,
        LocalDateTime createdAt
) {}
