package com.lottery.api.infrastructure.adapter.web.dto.response;

import com.fasterxml.jackson.databind.JsonNode;

public record AdminPredictionResponse(
        String id,
        String label,
        String savedAt,
        String latestDrawDate,
        JsonNode combos,
        String lotteryType,
        JsonNode generationParams,
        String userId
) {}
