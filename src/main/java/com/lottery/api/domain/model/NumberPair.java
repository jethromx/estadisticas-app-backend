package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NumberPair {
    int number1;
    int number2;
    long frequency;
    double percentage;
}
