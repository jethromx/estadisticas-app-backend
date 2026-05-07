package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.BayesianNumber;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.NumberFrequency;
import com.lottery.api.domain.port.in.GetBayesianAnalysisUseCase;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetBayesianAnalysisService implements GetBayesianAnalysisUseCase {

    private final LotteryDrawRepositoryPort repositoryPort;

    @Override
    @Cacheable(value = "analysis-bayesian", key = "#type.name() + '-' + #recentWindow")
    @Transactional(readOnly = true)
    public List<BayesianNumber> getBayesianAnalysis(LotteryType type, int recentWindow) {
        List<NumberFrequency> historicalFreqs = repositoryPort.getNumberFrequencies(type);
        List<NumberFrequency> recentFreqs     = repositoryPort.getFrequenciesByDrawWindow(type, recentWindow);

        long totalHistObs = historicalFreqs.stream().mapToLong(NumberFrequency::getFrequency).sum();
        // Total number slots in the recent window (approximate)
        long recentTotalSlots = (long) recentWindow * type.getTotalNumbers();

        Map<Integer, Long> recentMap = recentFreqs.stream()
                .collect(Collectors.toMap(NumberFrequency::getNumber, NumberFrequency::getFrequency));

        return historicalFreqs.stream().map(hf -> {
            long histFreq = hf.getFrequency();

            // Prior: Jeffreys-like Beta(alpha, beta)
            double alpha = histFreq + 1.0;
            double beta  = totalHistObs - histFreq + 1.0;
            double priorMean = alpha / (alpha + beta);

            long recentFreq   = recentMap.getOrDefault(hf.getNumber(), 0L);
            long recentMisses = recentTotalSlots - recentFreq;

            // Bayesian update
            double postAlpha = alpha + recentFreq;
            double postBeta  = beta  + recentMisses;
            double posteriorMean = postAlpha / (postAlpha + postBeta);

            double lift = priorMean > 0 ? Math.round((posteriorMean / priorMean - 1) * 10000.0) / 100.0 : 0;

            return BayesianNumber.builder()
                    .number(hf.getNumber())
                    .posteriorMean(Math.round(posteriorMean * 1_000_000.0) / 1_000_000.0)
                    .priorMean(Math.round(priorMean * 1_000_000.0) / 1_000_000.0)
                    .historicalFrequency(histFreq)
                    .recentFrequency(recentFreq)
                    .recentWindow(recentWindow)
                    .lift(lift)
                    .build();
        })
        .sorted((a, b) -> Double.compare(b.getPosteriorMean(), a.getPosteriorMean()))
        .toList();
    }
}
