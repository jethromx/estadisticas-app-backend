package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PositionStats {
    int position;
    double mean;
    double stdDev;
    int min;
    int max;
    int p10;
    int p25;
    int p50;
    int p75;
    int p90;
    int recommendedMin;
    int recommendedMax;
}
