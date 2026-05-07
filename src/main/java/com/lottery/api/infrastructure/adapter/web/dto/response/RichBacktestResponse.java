package com.lottery.api.infrastructure.adapter.web.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RichBacktestResponse {
    private String lotteryType;
    private int testDraws;
    private int topK;
    private List<StrategyResultResponse> strategies;
    private String bestStrategy;
    private double bestHitRate;

    @Data
    public static class StrategyResultResponse {
        private String strategyName;
        private List<Integer> numbers;
        private Map<Integer, Integer> matchDistribution;
        private double hitRate;
        private double avgMatches;
        private double expectedRandomRate;
        private double vsRandom;
        private List<Double> rollingHitRates;
    }
}
