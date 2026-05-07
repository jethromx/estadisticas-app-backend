package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class WeightedNumber {
    int number;
    long rawFrequency;
    double rawFrequencyPct;
    Map<String, Double> weightedScores;
    int rankByFrequency;
    int rankByWeight099;
    int rankByWeight090;
}
