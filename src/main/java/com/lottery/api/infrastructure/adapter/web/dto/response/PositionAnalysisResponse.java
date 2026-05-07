package com.lottery.api.infrastructure.adapter.web.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class PositionAnalysisResponse {
    private String lotteryType;
    private int totalDraws;
    private List<PositionStatsResponse> positions;

    @Data
    public static class PositionStatsResponse {
        private int position;
        private double mean;
        private double stdDev;
        private int min;
        private int max;
        private int p10;
        private int p25;
        private int p50;
        private int p75;
        private int p90;
        private int recommendedMin;
        private int recommendedMax;
    }
}
