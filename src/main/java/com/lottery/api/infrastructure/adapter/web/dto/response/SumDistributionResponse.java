package com.lottery.api.infrastructure.adapter.web.dto.response;

import java.util.Map;

public record SumDistributionResponse(
        String lotteryType,
        Map<Integer, Long> histogram,
        double mean,
        double stdDev,
        int minSum,
        int maxSum,
        int optimalMin,
        int optimalMax,
        double p25,
        double p50,
        double p75,
        int totalDraws
) {}
