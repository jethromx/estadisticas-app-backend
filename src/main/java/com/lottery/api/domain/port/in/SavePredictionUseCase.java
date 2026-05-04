package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.SavePredictionCommand;
import com.lottery.api.domain.model.SavedPrediction;

public interface SavePredictionUseCase {
    SavedPrediction execute(SavePredictionCommand command);
}
