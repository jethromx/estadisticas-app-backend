package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class BalanceAnalysis {
    LotteryType lotteryType;
    /** Key = number of odd numbers in draw, value = how many draws had that split. */
    Map<Integer, Long> oddEvenDistribution;
    /** Key = number of high numbers in draw, value = how many draws had that split. */
    Map<Integer, Long> highLowDistribution;
    int optimalOddCount;
    int optimalEvenCount;
    int optimalHighCount;
    int optimalLowCount;
    int totalDraws;
    int numbersPerDraw;
    int midpoint;
}
