package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@Builder
public class BacktestResult {
    LotteryType lotteryType;
    String strategy;
    int topK;
    int totalDrawsTested;
    List<Integer> predictedNumbers;
    Map<Integer, Integer> matchDistribution;
    double avgMatches;
    double hitRate;
    double expectedRandomRate;
}
