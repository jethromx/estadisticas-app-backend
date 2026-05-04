package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.SavedPrediction;

import java.util.List;

public interface GetPredictionsUseCase {
    List<SavedPrediction> execute(String userId);
}
