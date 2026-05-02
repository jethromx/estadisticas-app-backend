package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.SumDistribution;

public interface GetSumDistributionUseCase {
    SumDistribution getSumDistribution(LotteryType lotteryType);
}
