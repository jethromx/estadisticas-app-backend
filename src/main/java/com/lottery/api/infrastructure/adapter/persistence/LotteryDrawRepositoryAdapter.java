package com.lottery.api.infrastructure.adapter.persistence;

import com.lottery.api.domain.model.BalanceAnalysis;
import com.lottery.api.domain.model.DueNumber;
import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.NumberFrequency;
import com.lottery.api.domain.model.NumberPair;
import com.lottery.api.domain.model.SumDistribution;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import com.lottery.api.infrastructure.adapter.persistence.mapper.LotteryPersistenceMapper;
import com.lottery.api.infrastructure.adapter.persistence.projection.BalanceProjection;
import com.lottery.api.infrastructure.adapter.persistence.projection.SumHistogramProjection;
import com.lottery.api.infrastructure.adapter.persistence.projection.SumStatsProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adaptador de salida que implementa {@link LotteryDrawRepositoryPort} usando JPA.
 *
 * <p>Actúa como puente entre el dominio y Spring Data, sin exponer JPA al resto
 * de capas (principio de inversión de dependencias).</p>
 */
@Component
@RequiredArgsConstructor
public class LotteryDrawRepositoryAdapter implements LotteryDrawRepositoryPort {

    private static final LocalDate EPOCH = LocalDate.of(1900, 1, 1);

    private final LotteryDrawJpaRepository jpaRepository;
    private final LotteryPersistenceMapper mapper;

    @Override
    public LotteryDraw save(LotteryDraw draw) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(draw)));
    }

    @Override
    public List<LotteryDraw> saveAll(List<LotteryDraw> draws) {
        return mapper.toDomainList(
                jpaRepository.saveAll(draws.stream().map(mapper::toEntity).toList()));
    }

    @Override
    public Optional<LotteryDraw> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<LotteryDraw> findByTypeAndDrawNumber(LotteryType type, Integer drawNumber) {
        return jpaRepository.findByLotteryTypeAndDrawNumber(type, drawNumber).map(mapper::toDomain);
    }

    @Override
    public List<LotteryDraw> findByType(LotteryType type) {
        return mapper.toDomainList(jpaRepository.findByLotteryTypeOrderByDrawDateDesc(type));
    }

    @Override
    public List<LotteryDraw> findByTypeAndDateRange(LotteryType type, LocalDate from, LocalDate to) {
        return mapper.toDomainList(jpaRepository.findByTypeAndDateRange(type, from, to));
    }

    @Override
    public List<LotteryDraw> findRecentByType(LotteryType type, int limit) {
        return mapper.toDomainList(jpaRepository.findRecentByType(type.name(), limit));
    }

    @Override
    public Page<LotteryDraw> findByTypePageable(LotteryType type, Pageable pageable) {
        return jpaRepository.findPageableByType(type, pageable).map(mapper::toDomain);
    }

    @Override
    public long countByType(LotteryType type) {
        return jpaRepository.countByLotteryType(type);
    }

    @Override
    public List<NumberFrequency> getNumberFrequencies(LotteryType type) {
        return getNumberFrequenciesInRange(type, EPOCH, LocalDate.now());
    }

    @Override
    public List<NumberFrequency> getNumberFrequenciesByDateRange(LotteryType type, LocalDate from, LocalDate to) {
        return getNumberFrequenciesInRange(type, from, to);
    }

    @Override
    public Optional<LocalDate> findFirstDrawDateByType(LotteryType type) {
        return jpaRepository.findMinDrawDateByType(type);
    }

    @Override
    public Optional<LocalDate> findLastDrawDateByType(LotteryType type) {
        return jpaRepository.findMaxDrawDateByType(type);
    }

    @Override
    public boolean existsByTypeAndDrawNumber(LotteryType type, Integer drawNumber) {
        return jpaRepository.existsByLotteryTypeAndDrawNumber(type, drawNumber);
    }

    @Override
    public Set<Integer> findAllDrawNumbersByType(LotteryType type) {
        return jpaRepository.findAllDrawNumbersByType(type);
    }

    @Override
    public List<DueNumber> getDueNumbers(LotteryType type, int limit) {
        return jpaRepository.findDueNumbers(type.name(), limit).stream()
                .map(p -> DueNumber.builder()
                        .number(p.getNumber())
                        .frequency(p.getFrequency())
                        .lastDrawNumber(p.getLastDrawNumber())
                        .drawsSinceLast(p.getDrawsSinceLast())
                        .avgInterval(p.getAvgInterval())
                        .dueScore(p.getDueScore())
                        .build())
                .toList();
    }

    @Override
    public List<NumberFrequency> getFrequenciesByDrawWindow(LotteryType type, int windowSize) {
        var projections = jpaRepository.findFrequenciesInWindow(type.name(), windowSize);
        return mapper.toNumberFrequencyList(projections, type);
    }

    @Override
    public long countDrawsInWindow(LotteryType type, int windowSize) {
        return jpaRepository.countDrawsInWindow(type.name(), windowSize);
    }

    @Override
    public BalanceAnalysis getBalanceAnalysis(LotteryType type) {
        int midpoint = (type.getMinNumber() + type.getMaxNumber()) / 2;
        List<BalanceProjection> rows = jpaRepository.findBalanceDistribution(type.name(), midpoint);

        Map<Integer, Long> oddEven = new LinkedHashMap<>();
        Map<Integer, Long> highLow = new LinkedHashMap<>();

        for (BalanceProjection row : rows) {
            oddEven.merge(row.getOddCount(), row.getDrawCount(), Long::sum);
            highLow.merge(row.getHighCount(), row.getDrawCount(), Long::sum);
        }

        int optimalOdd  = oddEven.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(0);
        int optimalHigh = highLow.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(0);
        long totalDraws = oddEven.values().stream().mapToLong(Long::longValue).sum();

        return BalanceAnalysis.builder()
                .lotteryType(type)
                .oddEvenDistribution(sortedByKey(oddEven))
                .highLowDistribution(sortedByKey(highLow))
                .optimalOddCount(optimalOdd)
                .optimalEvenCount(type.getNumbersCount() - optimalOdd)
                .optimalHighCount(optimalHigh)
                .optimalLowCount(type.getNumbersCount() - optimalHigh)
                .totalDraws((int) totalDraws)
                .numbersPerDraw(type.getNumbersCount())
                .midpoint(midpoint)
                .build();
    }

    @Override
    public SumDistribution getSumDistribution(LotteryType type) {
        List<SumHistogramProjection> histogram = jpaRepository.findSumHistogram(type.name());
        SumStatsProjection stats = jpaRepository.findSumStats(type.name());

        if (stats == null || stats.getMean() == null) {
            return SumDistribution.builder().lotteryType(type).histogram(Map.of())
                    .mean(0).stdDev(0).minSum(0).maxSum(0)
                    .optimalMin(0).optimalMax(0).p25(0).p50(0).p75(0).totalDraws(0).build();
        }

        double mean   = stats.getMean();
        double stdDev = stats.getStdDev() != null ? stats.getStdDev() : 0.0;

        Map<Integer, Long> hist = histogram.stream()
                .collect(Collectors.toMap(
                        SumHistogramProjection::getSumValue,
                        SumHistogramProjection::getFrequency,
                        Long::sum,
                        LinkedHashMap::new));

        return SumDistribution.builder()
                .lotteryType(type)
                .histogram(hist)
                .mean(mean)
                .stdDev(stdDev)
                .minSum(stats.getMinSum())
                .maxSum(stats.getMaxSum())
                .optimalMin((int) Math.floor(mean - stdDev))
                .optimalMax((int) Math.ceil(mean + stdDev))
                .p25(stats.getP25() != null ? stats.getP25() : 0)
                .p50(stats.getP50() != null ? stats.getP50() : 0)
                .p75(stats.getP75() != null ? stats.getP75() : 0)
                .totalDraws(stats.getTotalDraws().intValue())
                .build();
    }

    @Override
    public List<NumberPair> getPairFrequencies(LotteryType type, int limit) {
        long totalDraws = jpaRepository.countByLotteryType(type);
        int k = type.getNumbersCount();
        long totalPairInstances = totalDraws * (long) k * (k - 1) / 2;

        return jpaRepository.findTopPairs(type.name(), limit).stream()
                .map(p -> NumberPair.builder()
                        .number1(p.getNumber1())
                        .number2(p.getNumber2())
                        .frequency(p.getFrequency())
                        .percentage(totalPairInstances > 0
                                ? Math.round(p.getFrequency() * 10000.0 / totalPairInstances) / 100.0
                                : 0)
                        .build())
                .toList();
    }

    @Override
    public List<LotteryDraw> findDrawsAfterDate(LotteryType type, LocalDate afterDate) {
        LocalDate from = afterDate != null ? afterDate : EPOCH;
        return mapper.toDomainList(jpaRepository.findByTypeAndDrawDateAfter(type, from));
    }

    private List<NumberFrequency> getNumberFrequenciesInRange(LotteryType type, LocalDate from, LocalDate to) {
        var projections = jpaRepository.getNumberFrequenciesInRange(type.name(), from, to);
        return mapper.toNumberFrequencyList(projections, type);
    }

    private static Map<Integer, Long> sortedByKey(Map<Integer, Long> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        Long::sum, LinkedHashMap::new));
    }
}
