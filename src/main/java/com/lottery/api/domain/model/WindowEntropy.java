package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class WindowEntropy {
    int windowIndex;
    LocalDate startDate;
    LocalDate endDate;
    int drawCount;
    double entropy;
    double entropyRatio;
}
