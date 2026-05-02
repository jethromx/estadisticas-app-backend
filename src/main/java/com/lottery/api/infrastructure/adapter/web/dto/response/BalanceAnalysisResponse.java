package com.lottery.api.infrastructure.adapter.web.dto.response;

import java.util.Map;

public record BalanceAnalysisResponse(
        String lotteryType,
        Map<Integer, Long> oddEvenDistribution,
        Map<Integer, Long> highLowDistribution,
        int optimalOddCount,
        int optimalEvenCount,
        int optimalHighCount,
        int optimalLowCount,
        int totalDraws,
        int numbersPerDraw,
        int midpoint
) {}
