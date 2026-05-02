package com.lottery.api.infrastructure.adapter.web.dto.response;

import lombok.Data;

@Data
public class BayesianNumberResponse {
    private int number;
    private double posteriorMean;
    private double priorMean;
    private long historicalFrequency;
    private long recentFrequency;
    private int recentWindow;
    private double lift;
}
