package com.lottery.api.infrastructure.adapter.web.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ConsecutiveAnalysisResponse {
    private String lotteryType;
    private int totalDraws;
    private int drawsWithAtLeastOne;
    private double consecutiveRate;
    private Map<Integer, Long> distributionByCount;
    private double avgPairsPerDraw;
    private List<ConsecutivePairResponse> topPairs;

    @Data
    public static class ConsecutivePairResponse {
        private int lower;
        private int higher;
        private long frequency;
        private double percentage;
    }
}
