package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.DrawStreak;
import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.RecentDrawSum;
import com.lottery.api.domain.model.SumStreakAnalysis;
import com.lottery.api.domain.port.in.GetSumStreakUseCase;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetSumStreakService implements GetSumStreakUseCase {

    private final LotteryDrawRepositoryPort repositoryPort;

    @Override
    @Cacheable(value = "analysis-streak", key = "#type.name() + '-' + #recentDraws")
    @Transactional(readOnly = true)
    public SumStreakAnalysis getSumStreak(LotteryType type, int recentDraws) {
        List<LotteryDraw> draws = repositoryPort.findByType(type);
        // oldest-first
        List<LotteryDraw> ordered = new ArrayList<>(draws);
        Collections.reverse(ordered);

        int total = ordered.size();
        if (total == 0) {
            return SumStreakAnalysis.builder()
                    .lotteryType(type).totalDraws(0).meanSum(0).stdDevSum(0)
                    .longestHighStreak(0).longestLowStreak(0)
                    .recentDrawsSums(List.of()).streakHistory(List.of()).build();
        }

        int[] sums = new int[total];
        for (int i = 0; i < total; i++) {
            sums[i] = ordered.get(i).getNumbers() == null ? 0
                    : ordered.get(i).getNumbers().stream().mapToInt(Integer::intValue).sum();
        }

        double mean   = computeMean(sums);
        double stdDev = computeStdDev(sums, mean);

        // Build streak history (oldest to newest)
        List<DrawStreak> allStreaks = new ArrayList<>();
        int streakStart = 0;
        String streakType = sums[0] >= mean ? "HIGH" : "LOW";
        int longestHigh = 0, longestLow = 0;

        for (int i = 1; i <= total; i++) {
            boolean boundary = (i == total);
            if (!boundary) {
                String cur = sums[i] >= mean ? "HIGH" : "LOW";
                boundary = !cur.equals(streakType);
            }
            if (boundary) {
                int length = i - streakStart;
                LotteryDraw first = ordered.get(streakStart);
                LotteryDraw last  = ordered.get(i - 1);
                allStreaks.add(DrawStreak.builder()
                        .type(streakType).length(length)
                        .startDate(first.getDrawDate()).endDate(last.getDrawDate())
                        .startDrawNumber(first.getDrawNumber()).endDrawNumber(last.getDrawNumber())
                        .build());
                if ("HIGH".equals(streakType) && length > longestHigh) longestHigh = length;
                if ("LOW".equals(streakType)  && length > longestLow)  longestLow  = length;
                if (i < total) {
                    streakType = sums[i] >= mean ? "HIGH" : "LOW";
                    streakStart = i;
                }
            }
        }

        DrawStreak current = allStreaks.isEmpty() ? null : allStreaks.get(allStreaks.size() - 1);
        List<DrawStreak> history = allStreaks.size() > 20
                ? allStreaks.subList(allStreaks.size() - 20, allStreaks.size())
                : allStreaks;

        // Recent draw sums (newest first, limited to recentDraws)
        List<RecentDrawSum> recent = new ArrayList<>();
        int limit = Math.min(recentDraws, total);
        for (int i = total - 1; i >= total - limit; i--) {
            LotteryDraw d = ordered.get(i);
            recent.add(RecentDrawSum.builder()
                    .drawNumber(d.getDrawNumber())
                    .drawDate(d.getDrawDate())
                    .sum(sums[i])
                    .aboveMean(sums[i] >= mean)
                    .build());
        }

        return SumStreakAnalysis.builder()
                .lotteryType(type)
                .totalDraws(total)
                .meanSum(Math.round(mean * 100.0) / 100.0)
                .stdDevSum(Math.round(stdDev * 100.0) / 100.0)
                .currentStreak(current)
                .longestHighStreak(longestHigh)
                .longestLowStreak(longestLow)
                .recentDrawsSums(recent)
                .streakHistory(history)
                .build();
    }

    private double computeMean(int[] vals) {
        double s = 0;
        for (int v : vals) s += v;
        return s / vals.length;
    }

    private double computeStdDev(int[] vals, double mean) {
        double s = 0;
        for (int v : vals) s += (v - mean) * (v - mean);
        return Math.sqrt(s / vals.length);
    }
}
