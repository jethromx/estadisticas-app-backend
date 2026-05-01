package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.NumberFrequency;
import com.lottery.api.domain.port.in.GetHotNumbersUseCase;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio que identifica números calientes (alta frecuencia) y fríos (baja frecuencia).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetHotNumbersService implements GetHotNumbersUseCase {

    private final LotteryDrawRepositoryPort repositoryPort;

    @Override
    @Transactional(readOnly = true)
    public List<NumberFrequency> getHotNumbers(LotteryType lotteryType, int limit) {
        return repositoryPort.getNumberFrequencies(lotteryType).stream()
                .sorted(Comparator.comparingLong(NumberFrequency::getFrequency).reversed())
                .limit(limit)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NumberFrequency> getColdNumbers(LotteryType lotteryType, int limit) {
        return repositoryPort.getNumberFrequencies(lotteryType).stream()
                .sorted(Comparator.comparingLong(NumberFrequency::getFrequency))
                .limit(limit)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NumberFrequency> getRecentHotNumbers(LotteryType lotteryType, int recentDraws, int limit) {
        List<LotteryDraw> recent = repositoryPort.findRecentByType(lotteryType, recentDraws);

        Map<Integer, Long> freqMap = recent.stream()
                .flatMap(draw -> draw.getNumbers().stream())
                .collect(Collectors.groupingBy(n -> n, Collectors.counting()));

        long totalOccurrences = freqMap.values().stream().mapToLong(Long::longValue).sum();

        return freqMap.entrySet().stream()
                .map(e -> NumberFrequency.builder()
                        .number(e.getKey())
                        .frequency(e.getValue())
                        .percentage(totalOccurrences > 0
                                ? (e.getValue() * 100.0) / totalOccurrences
                                : 0.0)
                        .build())
                .sorted(Comparator.comparingLong(NumberFrequency::getFrequency).reversed())
                .limit(limit)
                .toList();
    }
}
