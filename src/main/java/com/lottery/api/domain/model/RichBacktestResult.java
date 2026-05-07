package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class RichBacktestResult {
    LotteryType lotteryType;
    int testDraws;
    int topK;
    List<StrategyResult> strategies;
    String bestStrategy;
    double bestHitRate;
}
