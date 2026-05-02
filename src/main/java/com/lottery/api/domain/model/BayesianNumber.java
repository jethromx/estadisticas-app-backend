package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BayesianNumber {
    int number;
    double posteriorMean;
    double priorMean;
    long historicalFrequency;
    long recentFrequency;
    int recentWindow;
    double lift;
}
