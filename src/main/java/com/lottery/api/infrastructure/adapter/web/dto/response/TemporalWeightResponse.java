package com.lottery.api.infrastructure.adapter.web.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TemporalWeightResponse {
    private String lotteryType;
    private int totalDraws;
    private List<Double> decayFactors;
    private List<WeightedNumberResponse> numbers;
    private String recommendation;

    @Data
    public static class WeightedNumberResponse {
        private int number;
        private long rawFrequency;
        private double rawFrequencyPct;
        private Map<String, Double> weightedScores;
        private int rankByFrequency;
        private int rankByWeight099;
        private int rankByWeight090;
    }
}
