package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.RichBacktestResult;

public interface GetRichBacktestUseCase {
    RichBacktestResult getRichBacktest(LotteryType type, int topK, int testDraws);
}
