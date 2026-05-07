package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.ClusterAnalysis;
import com.lottery.api.domain.model.LotteryType;

public interface GetClusterAnalysisUseCase {
    ClusterAnalysis getClusterAnalysis(LotteryType type, int k);
}
