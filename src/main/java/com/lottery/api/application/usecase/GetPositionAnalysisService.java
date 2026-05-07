package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.PositionAnalysis;
import com.lottery.api.domain.model.PositionStats;
import com.lottery.api.domain.port.in.GetPositionAnalysisUseCase;
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
public class GetPositionAnalysisService implements GetPositionAnalysisUseCase {

    private final LotteryDrawRepositoryPort repositoryPort;

    @Override
    @Cacheable(value = "analysis-position", key = "#type.name()")
    @Transactional(readOnly = true)
    public PositionAnalysis getPositionAnalysis(LotteryType type) {
        List<LotteryDraw> draws = repositoryPort.findByType(type);
        int numPositions = type.getNumbersCount();

        List<List<Integer>> byPosition = new ArrayList<>();
        for (int i = 0; i < numPositions; i++) byPosition.add(new ArrayList<>());

        for (LotteryDraw draw : draws) {
            if (draw.getNumbers() == null || draw.getNumbers().size() < numPositions) continue;
            List<Integer> sorted = new ArrayList<>(draw.getNumbers());
            Collections.sort(sorted);
            for (int p = 0; p < numPositions; p++) byPosition.get(p).add(sorted.get(p));
        }

        List<PositionStats> stats = new ArrayList<>();
        for (int p = 0; p < numPositions; p++) {
            stats.add(buildStats(p + 1, byPosition.get(p)));
        }

        return PositionAnalysis.builder()
                .lotteryType(type)
                .totalDraws(draws.size())
                .positions(stats)
                .build();
    }

    private PositionStats buildStats(int position, List<Integer> values) {
        if (values.isEmpty()) {
            return PositionStats.builder().position(position).build();
        }
        List<Integer> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int n = sorted.size();

        double mean = sorted.stream().mapToInt(Integer::intValue).average().orElse(0);
        double variance = sorted.stream().mapToDouble(v -> (v - mean) * (v - mean)).average().orElse(0);
        double stdDev = Math.sqrt(variance);

        int p10 = sorted.get((int) Math.max(0, Math.floor(n * 0.10)));
        int p25 = sorted.get((int) Math.max(0, Math.floor(n * 0.25)));
        int p50 = sorted.get((int) Math.max(0, Math.floor(n * 0.50)));
        int p75 = sorted.get((int) Math.min(n - 1, Math.floor(n * 0.75)));
        int p90 = sorted.get((int) Math.min(n - 1, Math.floor(n * 0.90)));

        return PositionStats.builder()
                .position(position)
                .mean(Math.round(mean * 100.0) / 100.0)
                .stdDev(Math.round(stdDev * 100.0) / 100.0)
                .min(sorted.get(0))
                .max(sorted.get(n - 1))
                .p10(p10).p25(p25).p50(p50).p75(p75).p90(p90)
                .recommendedMin(p10)
                .recommendedMax(p90)
                .build();
    }
}
