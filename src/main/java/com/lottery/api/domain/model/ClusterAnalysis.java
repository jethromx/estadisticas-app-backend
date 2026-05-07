package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ClusterAnalysis {
    LotteryType lotteryType;
    int totalDraws;
    int k;
    List<DrawCluster> clusters;
    String interpretation;
}
