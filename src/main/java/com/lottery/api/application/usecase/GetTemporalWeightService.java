package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.TemporalWeightResult;
import com.lottery.api.domain.model.WeightedNumber;
import com.lottery.api.domain.port.in.GetTemporalWeightUseCase;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GetTemporalWeightService implements GetTemporalWeightUseCase {

    private static final List<Double> DECAY_FACTORS = List.of(0.99, 0.97, 0.95, 0.90);

    private final LotteryDrawRepositoryPort repositoryPort;

    @Override
    @Cacheable(value = "analysis-temporal", key = "#type.name()")
    @Transactional(readOnly = true)
    public TemporalWeightResult getTemporalWeights(LotteryType type) {
        // findByType returns draws newest-first; reverse so index 0 = oldest
        List<LotteryDraw> draws = repositoryPort.findByType(type);
        List<LotteryDraw> oldest = new ArrayList<>(draws);
        java.util.Collections.reverse(oldest);

        int total = oldest.size();
        int maxNum = type.getMaxNumber();

        // Accumulate raw frequency
        Map<Integer, Long> rawFreq = new HashMap<>();

        // Per-decay weighted score: score[decay][number]
        Map<Double, Map<Integer, Double>> decayScores = new LinkedHashMap<>();
        for (double d : DECAY_FACTORS) decayScores.put(d, new HashMap<>());

        for (int age = 0; age < oldest.size(); age++) {
            LotteryDraw draw = oldest.get(age);
            if (draw.getNumbers() == null) continue;
            // age 0 = oldest draw; most recent draw has age = total-1
            // weight = decay^(total - 1 - age) so recent draws get higher weight
            int recency = total - 1 - age;
            for (int num : draw.getNumbers()) {
                rawFreq.merge(num, 1L, Long::sum);
                for (double d : DECAY_FACTORS) {
                    double weight = Math.pow(d, recency);
                    decayScores.get(d).merge(num, weight, Double::sum);
                }
            }
        }

        // Normalize each decay's scores to [0,1]
        Map<Double, Double> maxScore = new LinkedHashMap<>();
        for (double d : DECAY_FACTORS) {
            maxScore.put(d, decayScores.get(d).values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0));
        }

        long totalObs = rawFreq.values().stream().mapToLong(Long::longValue).sum();

        // Build ranked list by raw frequency
        List<Integer> byFreq = rawFreq.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey).toList();
        List<Integer> by099 = decayScores.get(0.99).entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey).toList();
        List<Integer> by090 = decayScores.get(0.90).entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey).toList();

        List<WeightedNumber> numbers = new ArrayList<>();
        for (int num = type.getMinNumber(); num <= maxNum; num++) {
            long freq = rawFreq.getOrDefault(num, 0L);
            double freqPct = totalObs > 0 ? Math.round(freq * 10000.0 / totalObs) / 100.0 : 0;

            Map<String, Double> scores = new LinkedHashMap<>();
            for (double d : DECAY_FACTORS) {
                double raw = decayScores.get(d).getOrDefault(num, 0.0);
                double max = maxScore.getOrDefault(d, 1.0);
                scores.put(String.valueOf(d), Math.round(raw / max * 10000.0) / 10000.0);
            }

            int n = num;
            numbers.add(WeightedNumber.builder()
                    .number(num)
                    .rawFrequency(freq)
                    .rawFrequencyPct(freqPct)
                    .weightedScores(scores)
                    .rankByFrequency(byFreq.indexOf(n) + 1)
                    .rankByWeight099(by099.indexOf(n) + 1)
                    .rankByWeight090(by090.indexOf(n) + 1)
                    .build());
        }

        numbers.sort((a, b) -> Double.compare(
                b.getWeightedScores().getOrDefault("0.97", 0.0),
                a.getWeightedScores().getOrDefault("0.97", 0.0)));

        return TemporalWeightResult.builder()
                .lotteryType(type)
                .totalDraws(total)
                .decayFactors(DECAY_FACTORS)
                .numbers(numbers)
                .recommendation("Los números con mayor score ponderado (decay=0.97) fueron más frecuentes en sorteos recientes. Considera combinar con análisis de due-numbers para máxima cobertura.")
                .build();
    }
}
