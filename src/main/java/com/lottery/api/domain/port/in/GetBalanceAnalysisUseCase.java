package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.BalanceAnalysis;
import com.lottery.api.domain.model.LotteryType;

public interface GetBalanceAnalysisUseCase {
    BalanceAnalysis getBalanceAnalysis(LotteryType lotteryType);
}
