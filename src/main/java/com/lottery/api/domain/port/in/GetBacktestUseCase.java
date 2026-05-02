package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.BacktestResult;
import com.lottery.api.domain.model.LotteryType;

public interface GetBacktestUseCase {
    BacktestResult getBacktest(LotteryType type, int topK, int testDraws);
}
