package com.lottery.api.infrastructure.adapter.web.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class SumStreakResponse {
    private String lotteryType;
    private int totalDraws;
    private double meanSum;
    private double stdDevSum;
    private DrawStreakResponse currentStreak;
    private int longestHighStreak;
    private int longestLowStreak;
    private List<RecentDrawSumResponse> recentDrawsSums;
    private List<DrawStreakResponse> streakHistory;

    @Data
    public static class DrawStreakResponse {
        private String type;
        private int length;
        private LocalDate startDate;
        private LocalDate endDate;
        private int startDrawNumber;
        private int endDrawNumber;
    }

    @Data
    public static class RecentDrawSumResponse {
        private int drawNumber;
        private LocalDate drawDate;
        private int sum;
        private boolean aboveMean;
    }
}
