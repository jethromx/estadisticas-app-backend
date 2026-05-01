package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.LotteryStatistics;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.NumberFrequency;
import com.lottery.api.domain.port.in.GetStatisticsUseCase;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Servicio que calcula estadísticas agregadas a partir de las frecuencias del repositorio.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetStatisticsService implements GetStatisticsUseCase {

    private static final int TOP_NUMBERS_LIMIT = 10;

    private final LotteryDrawRepositoryPort repositoryPort;

    @Override
    @Transactional(readOnly = true)
    public LotteryStatistics getStatistics(LotteryType lotteryType) {
        log.debug("Calculando estadísticas para: {}", lotteryType);
        List<NumberFrequency> frequencies = repositoryPort.getNumberFrequencies(lotteryType);
        return buildStatistics(lotteryType, frequencies);
    }

    @Override
    @Transactional(readOnly = true)
    public LotteryStatistics getStatisticsByDateRange(LotteryType lotteryType, LocalDate from, LocalDate to) {
        log.debug("Calculando estadísticas para {} entre {} y {}", lotteryType, from, to);
        List<NumberFrequency> frequencies =
                repositoryPort.getNumberFrequenciesByDateRange(lotteryType, from, to);
        return buildStatistics(lotteryType, frequencies);
    }

    private LotteryStatistics buildStatistics(LotteryType lotteryType, List<NumberFrequency> frequencies) {
        long totalDraws = repositoryPort.countByType(lotteryType);
        LocalDate firstDate = repositoryPort.findFirstDrawDateByType(lotteryType).orElse(null);
        LocalDate lastDate  = repositoryPort.findLastDrawDateByType(lotteryType).orElse(null);

        double avgFrequency = frequencies.stream()
                .mapToLong(NumberFrequency::getFrequency)
                .average()
                .orElse(0.0);

        List<NumberFrequency> mostFrequent = frequencies.stream()
                .sorted(Comparator.comparingLong(NumberFrequency::getFrequency).reversed())
                .limit(TOP_NUMBERS_LIMIT)
                .toList();

        List<NumberFrequency> leastFrequent = frequencies.stream()
                .sorted(Comparator.comparingLong(NumberFrequency::getFrequency))
                .limit(TOP_NUMBERS_LIMIT)
                .toList();

        Map<Integer, Long> distribution = frequencies.stream()
                .collect(Collectors.toMap(NumberFrequency::getNumber, NumberFrequency::getFrequency));

        List<Integer> neverDrawn = IntStream
                .rangeClosed(lotteryType.getMinNumber(), lotteryType.getMaxNumber())
                .filter(n -> !distribution.containsKey(n) || distribution.get(n) == 0)
                .boxed()
                .toList();

        return LotteryStatistics.builder()
                .lotteryType(lotteryType)
                .totalDraws(totalDraws)
                .firstDrawDate(firstDate)
                .lastDrawDate(lastDate)
                .mostFrequent(mostFrequent)
                .leastFrequent(leastFrequent)
                .frequencyDistribution(distribution)
                .averageFrequency(avgFrequency)
                .numbersNeverDrawn(neverDrawn)
                .build();
    }
}
