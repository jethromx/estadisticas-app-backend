package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class PredictionAccuracyResult {
    String predictionId;
    String predictionLabel;
    LotteryType lotteryType;
    int drawsAnalyzed;
    int bestMatchCount;
    int worstMatchCount;
    double averageMatchCount;
    List<ComboMatchDetail> comboDetails;
    List<String> improvementSuggestions;
}
