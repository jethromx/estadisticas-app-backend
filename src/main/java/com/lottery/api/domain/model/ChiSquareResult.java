package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ChiSquareResult {
    LotteryType lotteryType;
    double chiSquare;
    int degreesOfFreedom;
    double pValue;
    long totalObservations;
    double expectedFrequency;
    String interpretation;
}
