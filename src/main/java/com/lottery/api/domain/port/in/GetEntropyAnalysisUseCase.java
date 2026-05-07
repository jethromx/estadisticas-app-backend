package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.EntropyAnalysis;
import com.lottery.api.domain.model.LotteryType;

public interface GetEntropyAnalysisUseCase {
    EntropyAnalysis getEntropyAnalysis(LotteryType type, int windowSize);
}
