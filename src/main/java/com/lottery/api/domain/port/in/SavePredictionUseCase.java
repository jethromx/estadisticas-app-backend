package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.SavedPrediction;

public interface SavePredictionUseCase {
    SavedPrediction execute(String label, String latestDrawDate, String combosJson);
}
