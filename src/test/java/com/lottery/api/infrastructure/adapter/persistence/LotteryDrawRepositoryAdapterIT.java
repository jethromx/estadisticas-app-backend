package com.lottery.api.infrastructure.adapter.persistence;

import com.lottery.api.TestcontainersConfig;
import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.NumberFrequency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de integración del adaptador de persistencia contra PostgreSQL real
 * levantado por Testcontainers.
 */
@SpringBootTest
@Import(TestcontainersConfig.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("LotteryDrawRepositoryAdapter — Tests de Integración")
class LotteryDrawRepositoryAdapterIT {

    @Autowired
    private LotteryDrawRepositoryAdapter adapter;

    private LotteryDraw melateDraw1;
    private LotteryDraw melateDraw2;

    @BeforeEach
    void setUp() {
        melateDraw1 = LotteryDraw.builder()
                .lotteryType(LotteryType.MELATE)
                .drawNumber(100)
                .drawDate(LocalDate.of(2025, 1, 10))
                .numbers(List.of(5, 15, 25, 35, 45, 55))
                .additionalNumber(10)
                .jackpotAmount(new BigDecimal("50000000"))
                .build();

        melateDraw2 = LotteryDraw.builder()
                .lotteryType(LotteryType.MELATE)
                .drawNumber(101)
                .drawDate(LocalDate.of(2025, 1, 13))
                .numbers(List.of(1, 11, 21, 31, 41, 51))
                .additionalNumber(20)
                .jackpotAmount(new BigDecimal("60000000"))
                .build();
    }

    @Test
    @DisplayName("save y findById deben persistir y recuperar un sorteo")
    void saveAndFindById_persistsAndRetrievesDraw() {
        LotteryDraw saved = adapter.save(melateDraw1);

        assertThat(saved.getId()).isNotNull();
        Optional<LotteryDraw> found = adapter.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDrawNumber()).isEqualTo(100);
        assertThat(found.get().getNumbers()).containsExactly(5, 15, 25, 35, 45, 55);
        assertThat(found.get().getAdditionalNumber()).isEqualTo(10);
    }

    @Test
    @DisplayName("existsByTypeAndDrawNumber detecta duplicados correctamente")
    void existsByTypeAndDrawNumber_detectsDuplicates() {
        adapter.save(melateDraw1);

        assertThat(adapter.existsByTypeAndDrawNumber(LotteryType.MELATE, 100)).isTrue();
        assertThat(adapter.existsByTypeAndDrawNumber(LotteryType.MELATE, 999)).isFalse();
        assertThat(adapter.existsByTypeAndDrawNumber(LotteryType.REVANCHA, 100)).isFalse();
    }

    @Test
    @DisplayName("findByType devuelve todos los sorteos del tipo indicado")
    void findByType_returnsOnlyRequestedType() {
        adapter.save(melateDraw1);
        adapter.save(melateDraw2);

        LotteryDraw revanchaDraw = LotteryDraw.builder()
                .lotteryType(LotteryType.REVANCHA)
                .drawNumber(100)
                .drawDate(LocalDate.of(2025, 1, 10))
                .numbers(List.of(2, 12, 22, 32, 42, 52))
                .build();
        adapter.save(revanchaDraw);

        List<LotteryDraw> melateDraws = adapter.findByType(LotteryType.MELATE);
        assertThat(melateDraws).hasSize(2);
        assertThat(melateDraws).allMatch(d -> d.getLotteryType() == LotteryType.MELATE);
    }

    @Test
    @DisplayName("countByType cuenta correctamente")
    void countByType_returnsCorrectCount() {
        adapter.save(melateDraw1);
        adapter.save(melateDraw2);

        assertThat(adapter.countByType(LotteryType.MELATE)).isEqualTo(2);
        assertThat(adapter.countByType(LotteryType.REVANCHA)).isEqualTo(0);
    }

    @Test
    @DisplayName("findRecentByType devuelve los N más recientes")
    void findRecentByType_returnsLimitedResults() {
        adapter.save(melateDraw1);
        adapter.save(melateDraw2);

        List<LotteryDraw> recent = adapter.findRecentByType(LotteryType.MELATE, 1);
        assertThat(recent).hasSize(1);
        assertThat(recent.get(0).getDrawNumber()).isEqualTo(101); // más reciente
    }

    @Test
    @DisplayName("findByTypeAndDateRange filtra por fechas correctamente")
    void findByTypeAndDateRange_filtersCorrectly() {
        adapter.save(melateDraw1); // 2025-01-10
        adapter.save(melateDraw2); // 2025-01-13

        List<LotteryDraw> draws = adapter.findByTypeAndDateRange(
                LotteryType.MELATE,
                LocalDate.of(2025, 1, 12),
                LocalDate.of(2025, 1, 31));

        assertThat(draws).hasSize(1);
        assertThat(draws.get(0).getDrawNumber()).isEqualTo(101);
    }

    @Test
    @DisplayName("getNumberFrequencies calcula frecuencias de todos los números")
    void getNumberFrequencies_calculatesCorrectFrequencies() {
        adapter.save(melateDraw1); // números: 5,15,25,35,45,55 + adicional 10
        adapter.save(melateDraw2); // números: 1,11,21,31,41,51 + adicional 20

        List<NumberFrequency> frequencies = adapter.getNumberFrequencies(LotteryType.MELATE);

        assertThat(frequencies).isNotEmpty();
        // número 5 debe aparecer 1 vez
        frequencies.stream().filter(f -> f.getNumber().equals(5))
                .findFirst()
                .ifPresent(f -> assertThat(f.getFrequency()).isEqualTo(1L));
    }

    @Test
    @DisplayName("findFirstDrawDateByType y findLastDrawDateByType retornan fechas correctas")
    void findDateBoundaries_returnsCorrectDates() {
        adapter.save(melateDraw1); // 2025-01-10
        adapter.save(melateDraw2); // 2025-01-13

        assertThat(adapter.findFirstDrawDateByType(LotteryType.MELATE))
                .contains(LocalDate.of(2025, 1, 10));
        assertThat(adapter.findLastDrawDateByType(LotteryType.MELATE))
                .contains(LocalDate.of(2025, 1, 13));
    }

    @Test
    @DisplayName("findFirstDrawDateByType sin datos retorna Optional.empty()")
    void findDateBoundaries_noData_returnsEmpty() {
        assertThat(adapter.findFirstDrawDateByType(LotteryType.REVANCHITA)).isEmpty();
        assertThat(adapter.findLastDrawDateByType(LotteryType.REVANCHITA)).isEmpty();
    }
}
