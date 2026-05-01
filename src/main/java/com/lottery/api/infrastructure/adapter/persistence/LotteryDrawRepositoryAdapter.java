package com.lottery.api.infrastructure.adapter.persistence;

import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.NumberFrequency;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import com.lottery.api.infrastructure.adapter.persistence.mapper.LotteryPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    private List<NumberFrequency> getNumberFrequenciesInRange(LotteryType type, LocalDate from, LocalDate to) {
        var projections = jpaRepository.getNumberFrequenciesInRange(type.name(), from, to);
        return mapper.toNumberFrequencyList(projections, type);
    }
}
