package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@Builder
public class StrategyResult {
    String strategyName;
    List<Integer> numbers;
    Map<Integer, Integer> matchDistribution;
    double hitRate;
    double avgMatches;
    double expectedRandomRate;
    double vsRandom;
    List<Double> rollingHitRates;
}
