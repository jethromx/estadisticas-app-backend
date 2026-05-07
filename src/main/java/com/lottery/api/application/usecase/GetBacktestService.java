package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.BacktestResult;
import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.NumberFrequency;
import com.lottery.api.domain.port.in.GetBacktestUseCase;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetBacktestService implements GetBacktestUseCase {

    private final LotteryDrawRepositoryPort repositoryPort;

    @Override
    @Cacheable(value = "analysis-backtest", key = "#type.name() + '-' + #topK + '-' + #testDraws")
    @Transactional(readOnly = true)
    public BacktestResult getBacktest(LotteryType type, int topK, int testDraws) {
        List<NumberFrequency> freqs = repositoryPort.getNumberFrequencies(type);

        List<Integer> predicted = freqs.stream()
                .sorted((a, b) -> Long.compare(b.getFrequency(), a.getFrequency()))
                .limit(topK)
                .map(NumberFrequency::getNumber)
                .toList();

        Set<Integer> predictedSet = predicted.stream().collect(Collectors.toUnmodifiableSet());

        List<LotteryDraw> testDrawsList = repositoryPort.findRecentByType(type, testDraws);

        Map<Integer, Integer> matchDistribution = new LinkedHashMap<>();
        for (int i = 0; i <= topK; i++) matchDistribution.put(i, 0);

        for (LotteryDraw draw : testDrawsList) {
            int matches = (int) draw.getNumbers().stream()
                    .filter(predictedSet::contains)
                    .count();
            matchDistribution.merge(Math.min(matches, topK), 1, (a, b) -> a + b);
        }

        int total = testDrawsList.size();
        double avgMatches = total > 0
                ? matchDistribution.entrySet().stream()
                        .mapToDouble(e -> (double) e.getKey() * e.getValue())
                        .sum() / total
                : 0;

        int atLeastOne = total - matchDistribution.getOrDefault(0, 0);
        double hitRate = total > 0 ? (double) atLeastOne / total : 0;

        int rangeSize = type.getMaxNumber() - type.getMinNumber() + 1;
        double expectedRandomRate = 1.0 - hypergeometricZeroProb(rangeSize, topK, type.getNumbersCount());

        return BacktestResult.builder()
                .lotteryType(type)
                .strategy("HOT_NUMBERS")
                .topK(topK)
                .totalDrawsTested(total)
                .predictedNumbers(predicted)
                .matchDistribution(matchDistribution)
                .avgMatches(Math.round(avgMatches * 100.0) / 100.0)
                .hitRate(Math.round(hitRate * 10000.0) / 10000.0)
                .expectedRandomRate(Math.round(expectedRandomRate * 10000.0) / 10000.0)
                .build();
    }

    /** P(0 matches) in hypergeometric(R, K, n): C(R-K,n)/C(R,n). */
    private double hypergeometricZeroProb(int R, int K, int n) {
        if (K >= R) return 0;
        double prob = 1.0;
        for (int i = 0; i < n; i++) {
            prob *= (double) (R - K - i) / (R - i);
        }
        return Math.max(0, prob);
    }
}
