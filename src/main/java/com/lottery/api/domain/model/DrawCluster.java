package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class DrawCluster {
    int clusterId;
    int drawCount;
    double centroidSum;
    double centroidOddCount;
    double centroidSpread;
    List<Integer> mostCommonNumbers;
    double pctOfTotal;
}
