package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class EntropyAnalysis {
    LotteryType lotteryType;
    int totalDraws;
    int distinctNumbers;
    double observedEntropy;
    double maxPossibleEntropy;
    double entropyRatio;
    String interpretation;
    List<WindowEntropy> entropyByWindow;
}
