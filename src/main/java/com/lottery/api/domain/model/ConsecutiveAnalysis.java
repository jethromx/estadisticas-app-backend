package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@Builder
public class ConsecutiveAnalysis {
    LotteryType lotteryType;
    int totalDraws;
    int drawsWithAtLeastOne;
    double consecutiveRate;
    Map<Integer, Long> distributionByCount;
    double avgPairsPerDraw;
    List<ConsecutivePair> topPairs;
}
