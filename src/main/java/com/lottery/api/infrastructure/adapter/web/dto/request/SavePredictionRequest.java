package com.lottery.api.infrastructure.adapter.web.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.lottery.api.domain.model.LotteryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SavePredictionRequest(
        @NotBlank String label,
        String latestDrawDate,
        @NotNull JsonNode combos,
        LotteryType lotteryType,
        JsonNode generationParams
) {}
