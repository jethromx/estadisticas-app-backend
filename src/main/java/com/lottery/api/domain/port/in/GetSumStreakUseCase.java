package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.SumStreakAnalysis;

public interface GetSumStreakUseCase {
    SumStreakAnalysis getSumStreak(LotteryType type, int recentDraws);
}
