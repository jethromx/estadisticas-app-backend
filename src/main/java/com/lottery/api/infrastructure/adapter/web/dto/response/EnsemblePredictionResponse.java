package com.lottery.api.infrastructure.adapter.web.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class EnsemblePredictionResponse {
    private String lotteryType;
    private int totalDrawsAnalyzed;
    private int validationDraws;
    private Map<String, Double> modelWeights;
    private double validationHitRate;
    private List<ScoredNumberResponse> scoredNumbers;
    private List<List<Integer>> suggestedCombos;
    private String methodDescription;

    @Data
    public static class ScoredNumberResponse {
        private int number;
        private double frequencyScore;
        private double recencyScore;
        private double dueScore;
        private double pairScore;
        private double compositeScore;
        private int rank;
    }
}
