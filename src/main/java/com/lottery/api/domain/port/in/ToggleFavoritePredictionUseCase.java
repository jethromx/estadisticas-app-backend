package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.SavedPrediction;

public interface ToggleFavoritePredictionUseCase {
    SavedPrediction execute(String predictionId, String userId);
}
