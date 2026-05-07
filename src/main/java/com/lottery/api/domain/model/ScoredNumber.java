package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ScoredNumber {
    int number;
    double frequencyScore;
    double recencyScore;
    double dueScore;
    double pairScore;
    double compositeScore;
    int rank;
}
