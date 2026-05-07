package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.DueNumber;
import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.NumberFrequency;
import com.lottery.api.domain.model.RichBacktestResult;
import com.lottery.api.domain.model.StrategyResult;
import com.lottery.api.domain.port.in.GetRichBacktestUseCase;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GetRichBacktestService implements GetRichBacktestUseCase {

    private final LotteryDrawRepositoryPort repositoryPort;

    @Override
    @Cacheable(value = "analysis-rich-backtest", key = "#type.name() + '-' + #topK + '-' + #testDraws")
    @Transactional(readOnly = true)
    public RichBacktestResult getRichBacktest(LotteryType type, int topK, int testDraws) {
        List<NumberFrequency> allFreqs = repositoryPort.getNumberFrequencies(type);
        List<LotteryDraw> recentDraws = repositoryPort.findRecentByType(type, testDraws);
        List<DueNumber> dueNumbers = repositoryPort.getDueNumbers(type, topK);

        int rangeSize = type.getMaxNumber() - type.getMinNumber() + 1;
        double expectedRandom = 1.0 - hypergeometricZeroProb(rangeSize, topK, type.getNumbersCount());

        List<Integer> hotNumbers = allFreqs.stream()
                .sorted(Comparator.comparingLong(NumberFrequency::getFrequency).reversed())
                .limit(topK).map(NumberFrequency::getNumber).toList();

        List<Integer> coldNumbers = allFreqs.stream()
                .sorted(Comparator.comparingLong(NumberFrequency::getFrequency))
                .limit(topK).map(NumberFrequency::getNumber).toList();

        List<Integer> dueNums = dueNumbers.stream()
                .limit(topK).map(DueNumber::getNumber).toList();

        List<Integer> balancedNumbers = buildBalanced(allFreqs, type, topK);

        List<StrategyResult> strategies = new ArrayList<>();
        strategies.add(buildResult("HOT_NUMBERS",   hotNumbers,     recentDraws, expectedRandom, type));
        strategies.add(buildResult("COLD_NUMBERS",  coldNumbers,    recentDraws, expectedRandom, type));
        strategies.add(buildResult("DUE_NUMBERS",   dueNums,        recentDraws, expectedRandom, type));
        strategies.add(buildResult("BALANCED",      balancedNumbers,recentDraws, expectedRandom, type));

        StrategyResult best = strategies.stream()
                .max(Comparator.comparingDouble(StrategyResult::getHitRate))
                .orElse(strategies.get(0));

        return RichBacktestResult.builder()
                .lotteryType(type)
                .testDraws(recentDraws.size())
                .topK(topK)
                .strategies(strategies)
                .bestStrategy(best.getStrategyName())
                .bestHitRate(best.getHitRate())
                .build();
    }

    private StrategyResult buildResult(String name, List<Integer> numbers,
                                       List<LotteryDraw> draws, double expectedRandom, LotteryType type) {
        Set<Integer> numSet = Set.copyOf(numbers);
        int total = draws.size();

        Map<Integer, Integer> distribution = new LinkedHashMap<>();
        for (int i = 0; i <= numbers.size(); i++) distribution.put(i, 0);

        int windowSize = Math.max(1, total / 10);
        List<Double> rollingHitRates = new ArrayList<>();
        int rollingHits = 0;

        for (int i = 0; i < total; i++) {
            LotteryDraw draw = draws.get(i);
            int matches = (int) draw.getNumbers().stream().filter(numSet::contains).count();
            distribution.merge(Math.min(matches, numbers.size()), 1, Integer::sum);

            if (matches > 0) rollingHits++;
            if ((i + 1) % windowSize == 0) {
                rollingHitRates.add(Math.round((double) rollingHits / windowSize * 10000.0) / 10000.0);
                rollingHits = 0;
            }
        }

        int atLeastOne = total - distribution.getOrDefault(0, 0);
        double hitRate = total > 0 ? Math.round((double) atLeastOne / total * 10000.0) / 10000.0 : 0;
        double avgMatches = total > 0
                ? Math.round(distribution.entrySet().stream()
                        .mapToDouble(e -> (double) e.getKey() * e.getValue()).sum() / total * 100.0) / 100.0
                : 0;
        double vsRandom = Math.round((hitRate - expectedRandom) * 10000.0) / 10000.0;

        return StrategyResult.builder()
                .strategyName(name)
                .numbers(numbers)
                .matchDistribution(distribution)
                .hitRate(hitRate)
                .avgMatches(avgMatches)
                .expectedRandomRate(Math.round(expectedRandom * 10000.0) / 10000.0)
                .vsRandom(vsRandom)
                .rollingHitRates(rollingHitRates)
                .build();
    }

    private List<Integer> buildBalanced(List<NumberFrequency> freqs, LotteryType type, int topK) {
        List<NumberFrequency> sorted = freqs.stream()
                .sorted(Comparator.comparingLong(NumberFrequency::getFrequency).reversed())
                .toList();
        List<Integer> odd  = sorted.stream().map(NumberFrequency::getNumber).filter(n -> n % 2 != 0).limit((long) Math.ceil(topK / 2.0)).toList();
        List<Integer> even = sorted.stream().map(NumberFrequency::getNumber).filter(n -> n % 2 == 0).limit((long) Math.floor(topK / 2.0)).toList();
        List<Integer> result = new ArrayList<>(odd);
        result.addAll(even);
        return result.stream().limit(topK).toList();
    }

    private double hypergeometricZeroProb(int R, int K, int n) {
        if (K >= R) return 0;
        double prob = 1.0;
        for (int i = 0; i < n; i++) prob *= (double) (R - K - i) / (R - i);
        return Math.max(0, prob);
    }
}
