package com.lottery.api.infrastructure.adapter.web.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class BacktestResponse {
    private String lotteryType;
    private String strategy;
    private int topK;
    private int totalDrawsTested;
    private List<Integer> predictedNumbers;
    private Map<Integer, Integer> matchDistribution;
    private double avgMatches;
    private double hitRate;
    private double expectedRandomRate;
}
