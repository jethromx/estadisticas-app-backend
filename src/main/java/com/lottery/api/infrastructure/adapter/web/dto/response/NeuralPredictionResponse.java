package com.lottery.api.infrastructure.adapter.web.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class NeuralPredictionResponse {
    private String lotteryType;
    private int totalDrawsAnalyzed;
    private int trainingDraws;
    private int validationDraws;
    private double validationHitRate;
    private int trainingEpochs;
    private List<ScoredNumberResponse> scoredNumbers;
    private List<List<Integer>> suggestedCombos;
    private String methodDescription;

    @Data
    public static class ScoredNumberResponse {
        private int    number;
        private double probability;
        private double recentFreq50;
        private double dueScore;
        private double trend;
        private int    rank;
    }
}
