package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WindowedFrequency {
    int number;
    int frequency;
    double percentage;
    int windowSize;
    int windowDrawCount;
    /** Positive = more frequent recently vs historical average; negative = less frequent. */
    double trend;
}
