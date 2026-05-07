package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class TemporalWeightResult {
    LotteryType lotteryType;
    int totalDraws;
    List<Double> decayFactors;
    List<WeightedNumber> numbers;
    String recommendation;
}
