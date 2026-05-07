package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ConsecutivePair {
    int lower;
    int higher;
    long frequency;
    double percentage;
}
