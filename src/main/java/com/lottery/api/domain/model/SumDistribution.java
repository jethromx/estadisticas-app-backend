package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class SumDistribution {
    LotteryType lotteryType;
    /** sum value -> number of draws with that exact sum */
    Map<Integer, Long> histogram;
    double mean;
    double stdDev;
    int minSum;
    int maxSum;
    /** Recommended lower bound: floor(mean - stdDev) */
    int optimalMin;
    /** Recommended upper bound: ceil(mean + stdDev) */
    int optimalMax;
    double p25;
    double p50;
    double p75;
    int totalDraws;
}
