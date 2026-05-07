package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class SumStreakAnalysis {
    LotteryType lotteryType;
    int totalDraws;
    double meanSum;
    double stdDevSum;
    DrawStreak currentStreak;
    int longestHighStreak;
    int longestLowStreak;
    List<RecentDrawSum> recentDrawsSums;
    List<DrawStreak> streakHistory;
}
