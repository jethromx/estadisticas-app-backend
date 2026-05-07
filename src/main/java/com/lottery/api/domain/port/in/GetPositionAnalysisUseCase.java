package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.PositionAnalysis;

public interface GetPositionAnalysisUseCase {
    PositionAnalysis getPositionAnalysis(LotteryType type);
}
