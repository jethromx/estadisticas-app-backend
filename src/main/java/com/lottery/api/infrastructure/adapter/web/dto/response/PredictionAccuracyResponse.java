package com.lottery.api.infrastructure.adapter.web.dto.response;

import java.util.List;

public record PredictionAccuracyResponse(
        String predictionId,
        String predictionLabel,
        String lotteryType,
        int drawsAnalyzed,
        int bestMatchCount,
        int worstMatchCount,
        double averageMatchCount,
        List<ComboMatchDetailResponse> comboDetails,
        List<String> improvementSuggestions
) {}
