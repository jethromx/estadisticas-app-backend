package com.lottery.api.infrastructure.adapter.web.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class EntropyAnalysisResponse {
    private String lotteryType;
    private int totalDraws;
    private int distinctNumbers;
    private double observedEntropy;
    private double maxPossibleEntropy;
    private double entropyRatio;
    private String interpretation;
    private List<WindowEntropyResponse> entropyByWindow;

    @Data
    public static class WindowEntropyResponse {
        private int windowIndex;
        private LocalDate startDate;
        private LocalDate endDate;
        private int drawCount;
        private double entropy;
        private double entropyRatio;
    }
}
