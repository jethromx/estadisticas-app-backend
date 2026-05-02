package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.NumberPair;

import java.util.List;

public interface GetPairAnalysisUseCase {
    List<NumberPair> getPairAnalysis(LotteryType type, int limit);
}
