package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.PredictionAccuracyResult;

public interface AnalyzePredictionAccuracyUseCase {
    PredictionAccuracyResult execute(String predictionId, String requestingUserId, boolean syncFirst);
}
