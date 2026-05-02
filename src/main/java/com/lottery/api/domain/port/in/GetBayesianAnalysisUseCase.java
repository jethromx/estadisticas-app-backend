package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.BayesianNumber;
import com.lottery.api.domain.model.LotteryType;

import java.util.List;

public interface GetBayesianAnalysisUseCase {
    List<BayesianNumber> getBayesianAnalysis(LotteryType type, int recentWindow);
}
