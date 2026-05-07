package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@Builder
public class EnsemblePrediction {
    LotteryType lotteryType;
    int totalDrawsAnalyzed;
    int validationDraws;
    Map<String, Double> modelWeights;
    double validationHitRate;
    List<ScoredNumber> scoredNumbers;
    List<List<Integer>> suggestedCombos;
    String methodDescription;
}
