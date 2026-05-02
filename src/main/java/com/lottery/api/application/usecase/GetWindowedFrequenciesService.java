package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.NumberFrequency;
import com.lottery.api.domain.model.WindowedFrequency;
import com.lottery.api.domain.port.in.GetWindowedFrequenciesUseCase;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetWindowedFrequenciesService implements GetWindowedFrequenciesUseCase {

    private final LotteryDrawRepositoryPort repositoryPort;

    @Override
    @Transactional(readOnly = true)
    public List<WindowedFrequency> getWindowedFrequencies(LotteryType lotteryType, int windowSize) {
        long totalDraws = repositoryPort.countByType(lotteryType);
        if (totalDraws == 0) return List.of();

        long windowDrawCount = repositoryPort.countDrawsInWindow(lotteryType, windowSize);
        if (windowDrawCount == 0) return List.of();

        Map<Integer, Double> historicalRates = repositoryPort.getNumberFrequencies(lotteryType)
                .stream()
                .collect(Collectors.toMap(
                        NumberFrequency::getNumber,
                        nf -> (double) nf.getFrequency() / totalDraws));

        return repositoryPort.getFrequenciesByDrawWindow(lotteryType, windowSize)
                .stream()
                .map((NumberFrequency nf) -> {
                    double windowRate     = (double) nf.getFrequency() / windowDrawCount;
                    double historicalRate = historicalRates.getOrDefault(nf.getNumber(), windowRate);
                    double trend = historicalRate > 0
                            ? Math.round((windowRate - historicalRate) / historicalRate * 10_000.0) / 100.0
                            : 0.0;
                    return WindowedFrequency.builder()
                            .number(nf.getNumber())
                            .frequency((int) nf.getFrequency())
                            .percentage(nf.getPercentage())
                            .windowSize(windowSize)
                            .windowDrawCount((int) windowDrawCount)
                            .trend(trend)
                            .build();
                })
                .sorted(Comparator.comparingInt(WindowedFrequency::getNumber))
                .collect(Collectors.toList());
    }
}
