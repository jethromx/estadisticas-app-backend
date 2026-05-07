package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class PositionAnalysis {
    LotteryType lotteryType;
    int totalDraws;
    List<PositionStats> positions;
}
