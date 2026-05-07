package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.TemporalWeightResult;

public interface GetTemporalWeightUseCase {
    TemporalWeightResult getTemporalWeights(LotteryType type);
}
