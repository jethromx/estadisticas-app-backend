package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.ConsecutiveAnalysis;
import com.lottery.api.domain.model.LotteryType;

public interface GetConsecutiveAnalysisUseCase {
    ConsecutiveAnalysis getConsecutiveAnalysis(LotteryType type, int topPairs);
}
