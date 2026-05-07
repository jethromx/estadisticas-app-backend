package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.NeuralPrediction;

public interface GetNeuralPredictionUseCase {
    NeuralPrediction getNeuralPrediction(LotteryType lotteryType);
}
