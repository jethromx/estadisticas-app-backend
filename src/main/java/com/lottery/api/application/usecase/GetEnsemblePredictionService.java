package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.EnsemblePrediction;
import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.ScoredNumber;
import com.lottery.api.domain.port.in.GetEnsemblePredictionUseCase;
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
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetEnsemblePredictionService implements GetEnsemblePredictionUseCase {

    private static final double DECAY = 0.97;
    // Candidate weight grids [freqW, recencyW, dueW, pairW] summing to 1
    private static final double[][] WEIGHT_GRID = {
        {0.25, 0.25, 0.25, 0.25}, {0.40, 0.20, 0.20, 0.20}, {0.20, 0.40, 0.20, 0.20},
        {0.20, 0.20, 0.40, 0.20}, {0.20, 0.20, 0.20, 0.40}, {0.35, 0.30, 0.20, 0.15},
        {0.30, 0.35, 0.20, 0.15}, {0.30, 0.25, 0.30, 0.15}, {0.35, 0.25, 0.25, 0.15},
        {0.25, 0.35, 0.25, 0.15}, {0.45, 0.25, 0.20, 0.10}, {0.20, 0.45, 0.25, 0.10},
    };

    private final LotteryDrawRepositoryPort repositoryPort;

    @Override
    @Cacheable(value = "analysis-ensemble", key = "#type.name() + '-' + #validationDraws")
    @Transactional(readOnly = true)
    public EnsemblePrediction getEnsemblePrediction(LotteryType type, int validationDraws) {
        List<LotteryDraw> allDraws = repositoryPort.findByType(type);
        List<LotteryDraw> oldest = new ArrayList<>(allDraws);
        Collections.reverse(oldest); // oldest-first

        int total = oldest.size();
        int valSize = Math.min(validationDraws, total / 5);
        int trainSize = total - valSize;

        List<LotteryDraw> training = oldest.subList(0, trainSize);
        List<LotteryDraw> validation = oldest.subList(trainSize, total);

        int k = type.getNumbersCount();

        // --- Feature 1: frequency score (normalized appearance rate) ---
        Map<Integer, Double> freqScore = computeFrequencyScore(training, type);

        // --- Feature 2: recency score (exponentially weighted) ---
        Map<Integer, Double> recencyScore = computeRecencyScore(training, type);

        // --- Feature 3: due score (draws since last / avg interval) ---
        Map<Integer, Double> dueScore = computeDueScore(training, type);

        // --- Feature 4: pair score (co-occurrence strength with other top numbers) ---
        Map<Integer, Double> pairScore = computePairScore(training, type, freqScore);

        // --- Grid search: find weights that maximize hit rate on validation ---
        double[] bestWeights = WEIGHT_GRID[0];
        double bestHitRate = -1;
        for (double[] weights : WEIGHT_GRID) {
            Map<Integer, Double> composite = composite(freqScore, recencyScore, dueScore, pairScore, weights, type);
            List<Integer> topK = topK(composite, k);
            Set<Integer> topSet = Set.copyOf(topK);
            long hits = validation.stream()
                    .filter(d -> d.getNumbers() != null && d.getNumbers().stream().anyMatch(topSet::contains))
                    .count();
            double rate = valSize > 0 ? (double) hits / valSize : 0;
            if (rate > bestHitRate) { bestHitRate = rate; bestWeights = weights; }
        }

        // --- Final scoring with best weights on all draws ---
        Map<Integer, Double> freqAll    = computeFrequencyScore(oldest, type);
        Map<Integer, Double> recencyAll = computeRecencyScore(oldest, type);
        Map<Integer, Double> dueAll     = computeDueScore(oldest, type);
        Map<Integer, Double> pairAll    = computePairScore(oldest, type, freqAll);
        Map<Integer, Double> finalComp  = composite(freqAll, recencyAll, dueAll, pairAll, bestWeights, type);

        List<Integer> ranked = topK(finalComp, type.getMaxNumber() - type.getMinNumber() + 1);
        List<ScoredNumber> scoredList = new ArrayList<>();
        for (int rank = 0; rank < ranked.size(); rank++) {
            int num = ranked.get(rank);
            scoredList.add(ScoredNumber.builder()
                    .number(num).rank(rank + 1)
                    .frequencyScore(round4(freqAll.getOrDefault(num, 0.0)))
                    .recencyScore(round4(recencyAll.getOrDefault(num, 0.0)))
                    .dueScore(round4(dueAll.getOrDefault(num, 0.0)))
                    .pairScore(round4(pairAll.getOrDefault(num, 0.0)))
                    .compositeScore(round4(finalComp.getOrDefault(num, 0.0)))
                    .build());
        }

        // Generate 5 non-overlapping combos from the top candidates
        List<List<Integer>> combos = generateCombos(ranked, k, 5);

        Map<String, Double> weights = new LinkedHashMap<>();
        weights.put("frequency", bestWeights[0]);
        weights.put("recency",   bestWeights[1]);
        weights.put("due",       bestWeights[2]);
        weights.put("pair",      bestWeights[3]);

        return EnsemblePrediction.builder()
                .lotteryType(type)
                .totalDrawsAnalyzed(total)
                .validationDraws(valSize)
                .modelWeights(weights)
                .validationHitRate(Math.round(bestHitRate * 10000.0) / 10000.0)
                .scoredNumbers(scoredList)
                .suggestedCombos(combos)
                .methodDescription("Modelo ensemble estadístico: combina frecuencia histórica, recencia " +
                        "exponencial (decay=" + DECAY + "), score de vencimiento y co-ocurrencia de pares. " +
                        "Los pesos óptimos se buscan por grid-search validado en los últimos " + valSize + " sorteos.")
                .build();
    }

    private Map<Integer, Double> computeFrequencyScore(List<LotteryDraw> draws, LotteryType type) {
        Map<Integer, Long> freq = new HashMap<>();
        for (LotteryDraw d : draws) {
            if (d.getNumbers() == null) continue;
            for (int n : d.getNumbers()) freq.merge(n, 1L, (a, b) -> a + b);
        }
        long max = freq.values().stream().mapToLong(Long::longValue).max().orElse(1L);
        Map<Integer, Double> scores = new HashMap<>();
        for (int n = type.getMinNumber(); n <= type.getMaxNumber(); n++)
            scores.put(n, max > 0 ? (double) freq.getOrDefault(n, 0L) / max : 0);
        return scores;
    }

    private Map<Integer, Double> computeRecencyScore(List<LotteryDraw> draws, LotteryType type) {
        Map<Integer, Double> score = new HashMap<>();
        int total = draws.size();
        for (int i = 0; i < total; i++) {
            LotteryDraw d = draws.get(i);
            if (d.getNumbers() == null) continue;
            double weight = Math.pow(DECAY, total - 1 - i);
            for (int n : d.getNumbers()) score.merge(n, weight, Double::sum);
        }
        double max = score.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        Map<Integer, Double> normalized = new HashMap<>();
        for (int n = type.getMinNumber(); n <= type.getMaxNumber(); n++)
            normalized.put(n, max > 0 ? score.getOrDefault(n, 0.0) / max : 0);
        return normalized;
    }

    private Map<Integer, Double> computeDueScore(List<LotteryDraw> draws, LotteryType type) {
        Map<Integer, Integer> lastSeen = new HashMap<>();
        Map<Integer, Long> freq = new HashMap<>();
        for (int i = 0; i < draws.size(); i++) {
            LotteryDraw d = draws.get(i);
            if (d.getNumbers() == null) continue;
            for (int n : d.getNumbers()) { lastSeen.put(n, i); freq.merge(n, 1L, (a, b) -> a + b); }
        }
        int last = draws.size() - 1;
        Map<Integer, Double> score = new HashMap<>();
        double maxDue = 0;
        for (int n = type.getMinNumber(); n <= type.getMaxNumber(); n++) {
            long f = freq.getOrDefault(n, 0L);
            if (f == 0) { score.put(n, 1.0); maxDue = Math.max(maxDue, 1.0); continue; }
            double avgInterval = (double) last / f;
            int since = last - lastSeen.getOrDefault(n, last);
            double due = avgInterval > 0 ? Math.max(0, since / avgInterval) : 0;
            score.put(n, due);
            maxDue = Math.max(maxDue, due);
        }
        double finalMax = maxDue > 0 ? maxDue : 1.0;
        score.replaceAll((n, v) -> v / finalMax);
        return score;
    }

    private Map<Integer, Double> computePairScore(List<LotteryDraw> draws, LotteryType type, Map<Integer, Double> freqScore) {
        // Co-occurrence with top-20 frequent numbers
        List<Integer> top20 = freqScore.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(20).map(Map.Entry::getKey).collect(Collectors.toList());
        Set<Integer> top20Set = Set.copyOf(top20);

        Map<Integer, Long> pairFreq = new HashMap<>();
        for (LotteryDraw d : draws) {
            if (d.getNumbers() == null) continue;
            for (int n : d.getNumbers()) {
                long coOccurrences = d.getNumbers().stream().filter(m -> m != n && top20Set.contains(m)).count();
                pairFreq.merge(n, coOccurrences, (a, b) -> a + b);
            }
        }
        long max = pairFreq.values().stream().mapToLong(Long::longValue).max().orElse(1L);
        Map<Integer, Double> scores = new HashMap<>();
        for (int n = type.getMinNumber(); n <= type.getMaxNumber(); n++)
            scores.put(n, max > 0 ? (double) pairFreq.getOrDefault(n, 0L) / max : 0);
        return scores;
    }

    private Map<Integer, Double> composite(
            Map<Integer, Double> freq, Map<Integer, Double> recency,
            Map<Integer, Double> due, Map<Integer, Double> pair,
            double[] weights, LotteryType type) {
        Map<Integer, Double> result = new HashMap<>();
        for (int n = type.getMinNumber(); n <= type.getMaxNumber(); n++) {
            double score = weights[0] * freq.getOrDefault(n, 0.0)
                         + weights[1] * recency.getOrDefault(n, 0.0)
                         + weights[2] * due.getOrDefault(n, 0.0)
                         + weights[3] * pair.getOrDefault(n, 0.0);
            result.put(n, score);
        }
        return result;
    }

    private List<Integer> topK(Map<Integer, Double> scores, int k) {
        return scores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(k).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    private List<List<Integer>> generateCombos(List<Integer> ranked, int comboSize, int numCombos) {
        List<List<Integer>> combos = new ArrayList<>();
        Random rng = new Random(42L);
        int poolSize = Math.min(ranked.size(), comboSize * numCombos);
        List<Integer> pool = ranked.subList(0, poolSize);

        for (int i = 0; i < numCombos; i++) {
            List<Integer> combo = new ArrayList<>(pool.subList(i * comboSize, Math.min((i + 1) * comboSize, pool.size())));
            if (combo.size() < comboSize) {
                List<Integer> extra = new ArrayList<>(ranked.subList(poolSize, ranked.size()));
                Collections.shuffle(extra, rng);
                combo.addAll(extra.subList(0, Math.min(comboSize - combo.size(), extra.size())));
            }
            Collections.sort(combo);
            combos.add(combo);
        }
        return combos;
    }

    private double round4(double v) { return Math.round(v * 10000.0) / 10000.0; }
}
