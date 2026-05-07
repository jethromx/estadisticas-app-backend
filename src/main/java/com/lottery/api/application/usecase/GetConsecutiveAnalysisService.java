package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.ConsecutiveAnalysis;
import com.lottery.api.domain.model.ConsecutivePair;
import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.port.in.GetConsecutiveAnalysisUseCase;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GetConsecutiveAnalysisService implements GetConsecutiveAnalysisUseCase {

    private final LotteryDrawRepositoryPort repositoryPort;

    @Override
    @Cacheable(value = "analysis-consecutive", key = "#type.name() + '-' + #topPairs")
    @Transactional(readOnly = true)
    public ConsecutiveAnalysis getConsecutiveAnalysis(LotteryType type, int topPairs) {
        List<LotteryDraw> draws = repositoryPort.findByType(type);
        int total = draws.size();

        Map<String, Long> pairCounts = new HashMap<>();
        Map<Integer, Long> distribution = new HashMap<>();
        int drawsWithAtLeastOne = 0;
        long totalPairs = 0;

        for (LotteryDraw draw : draws) {
            if (draw.getNumbers() == null || draw.getNumbers().isEmpty()) continue;
            List<Integer> sorted = new ArrayList<>(draw.getNumbers());
            Collections.sort(sorted);

            int pairsInDraw = 0;
            for (int i = 0; i < sorted.size() - 1; i++) {
                if (sorted.get(i + 1) - sorted.get(i) == 1) {
                    pairsInDraw++;
                    String key = sorted.get(i) + "-" + sorted.get(i + 1);
                    pairCounts.merge(key, 1L, Long::sum);
                }
            }
            if (pairsInDraw > 0) drawsWithAtLeastOne++;
            totalPairs += pairsInDraw;
            distribution.merge(pairsInDraw, 1L, Long::sum);
        }

        // Ensure 0 key always present
        distribution.putIfAbsent(0, (long) (total - drawsWithAtLeastOne));

        Map<Integer, Long> sortedDist = new LinkedHashMap<>();
        distribution.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> sortedDist.put(e.getKey(), e.getValue()));

        List<ConsecutivePair> topList = pairCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(topPairs)
                .map(e -> {
                    String[] parts = e.getKey().split("-");
                    int lower = Integer.parseInt(parts[0]);
                    return ConsecutivePair.builder()
                            .lower(lower).higher(lower + 1)
                            .frequency(e.getValue())
                            .percentage(total > 0 ? Math.round(e.getValue() * 10000.0 / total) / 100.0 : 0)
                            .build();
                })
                .toList();

        double consecutiveRate = total > 0 ? Math.round((double) drawsWithAtLeastOne / total * 10000.0) / 100.0 : 0;
        double avg = total > 0 ? Math.round((double) totalPairs / total * 100.0) / 100.0 : 0;

        return ConsecutiveAnalysis.builder()
                .lotteryType(type)
                .totalDraws(total)
                .drawsWithAtLeastOne(drawsWithAtLeastOne)
                .consecutiveRate(consecutiveRate)
                .distributionByCount(sortedDist)
                .avgPairsPerDraw(avg)
                .topPairs(topList)
                .build();
    }
}
