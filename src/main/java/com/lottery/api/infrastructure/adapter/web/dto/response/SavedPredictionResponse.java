package com.lottery.api.infrastructure.adapter.web.dto.response;

import com.fasterxml.jackson.annotation.JsonRawValue;

public record SavedPredictionResponse(
        String id,
        String label,
        String savedAt,
        String latestDrawDate,
        @JsonRawValue String combos
) {}
