package com.lottery.api.infrastructure.adapter.web.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ClusterAnalysisResponse {
    private String lotteryType;
    private int totalDraws;
    private int k;
    private List<DrawClusterResponse> clusters;
    private String interpretation;

    @Data
    public static class DrawClusterResponse {
        private int clusterId;
        private int drawCount;
        private double centroidSum;
        private double centroidOddCount;
        private double centroidSpread;
        private List<Integer> mostCommonNumbers;
        private double pctOfTotal;
    }
}
