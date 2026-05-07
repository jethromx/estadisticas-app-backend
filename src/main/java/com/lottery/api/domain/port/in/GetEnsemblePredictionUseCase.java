package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.EnsemblePrediction;
import com.lottery.api.domain.model.LotteryType;

public interface GetEnsemblePredictionUseCase {
    EnsemblePrediction getEnsemblePrediction(LotteryType type, int validationDraws);
}
