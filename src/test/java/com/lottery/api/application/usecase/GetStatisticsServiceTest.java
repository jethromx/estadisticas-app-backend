package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.LotteryStatistics;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.NumberFrequency;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetStatisticsService — Tests Unitarios")
class GetStatisticsServiceTest {

    @Mock private LotteryDrawRepositoryPort repositoryPort;
    @InjectMocks private GetStatisticsService service;

    private List<NumberFrequency> buildFrequencies() {
        return List.of(
                NumberFrequency.builder().number(1).frequency(100L).lastDrawnDate(LocalDate.of(2026, 1, 7)).build(),
                NumberFrequency.builder().number(2).frequency(80L).lastDrawnDate(LocalDate.of(2026, 1, 4)).build(),
                NumberFrequency.builder().number(3).frequency(120L).lastDrawnDate(LocalDate.of(2025, 12, 31)).build(),
                NumberFrequency.builder().number(55).frequency(50L).lastDrawnDate(LocalDate.of(2025, 6, 1)).build(),
                NumberFrequency.builder().number(56).frequency(30L).lastDrawnDate(LocalDate.of(2025, 1, 1)).build()
        );
    }

    @Test
    @DisplayName("debe calcular estadísticas completas correctamente")
    void getStatistics_returnsCorrectAggregation() {
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(buildFrequencies());
        when(repositoryPort.countByType(LotteryType.MELATE)).thenReturn(4158L);
        when(repositoryPort.findFirstDrawDateByType(LotteryType.MELATE)).thenReturn(Optional.of(LocalDate.of(1990, 1, 1)));
        when(repositoryPort.findLastDrawDateByType(LotteryType.MELATE)).thenReturn(Optional.of(LocalDate.of(2026, 1, 7)));

        LotteryStatistics stats = service.getStatistics(LotteryType.MELATE);

        assertThat(stats.getLotteryType()).isEqualTo(LotteryType.MELATE);
        assertThat(stats.getTotalDraws()).isEqualTo(4158L);
        assertThat(stats.getFirstDrawDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(stats.getLastDrawDate()).isEqualTo(LocalDate.of(2026, 1, 7));
        assertThat(stats.getMostFrequent()).isNotEmpty();
        assertThat(stats.getMostFrequent().get(0).getNumber()).isEqualTo(3); // mayor frecuencia
        assertThat(stats.getLeastFrequent().get(0).getNumber()).isEqualTo(56); // menor frecuencia
        assertThat(stats.getAverageFrequency()).isGreaterThan(0);
    }

    @Test
    @DisplayName("debe calcular números nunca sorteados")
    void getStatistics_calculatesNeverDrawn() {
        // Solo números 1,2,3 tienen frecuencia; 4-56 nunca sorteados
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(List.of(
                NumberFrequency.builder().number(1).frequency(100L).build(),
                NumberFrequency.builder().number(2).frequency(50L).build()
        ));
        when(repositoryPort.countByType(LotteryType.MELATE)).thenReturn(100L);
        when(repositoryPort.findFirstDrawDateByType(any())).thenReturn(Optional.empty());
        when(repositoryPort.findLastDrawDateByType(any())).thenReturn(Optional.empty());

        LotteryStatistics stats = service.getStatistics(LotteryType.MELATE);

        assertThat(stats.getNumbersNeverDrawn()).contains(3, 4, 10, 56);
        assertThat(stats.getNumbersNeverDrawn()).doesNotContain(1, 2);
    }

    @Test
    @DisplayName("debe filtrar por rango de fechas")
    void getStatisticsByDateRange_callsDateRangeQuery() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to   = LocalDate.of(2025, 12, 31);
        when(repositoryPort.getNumberFrequenciesByDateRange(LotteryType.REVANCHITA, from, to))
                .thenReturn(buildFrequencies());
        when(repositoryPort.countByType(LotteryType.REVANCHITA)).thenReturn(52L);
        when(repositoryPort.findFirstDrawDateByType(any())).thenReturn(Optional.of(from));
        when(repositoryPort.findLastDrawDateByType(any())).thenReturn(Optional.of(to));

        LotteryStatistics stats = service.getStatisticsByDateRange(LotteryType.REVANCHITA, from, to);

        assertThat(stats.getLotteryType()).isEqualTo(LotteryType.REVANCHITA);
        assertThat(stats.getTotalDraws()).isEqualTo(52L);
    }

    @Test
    @DisplayName("debe retornar promedioFrequency 0 cuando no hay datos")
    void getStatistics_noData_returnsZeroAverage() {
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(List.of());
        when(repositoryPort.countByType(LotteryType.MELATE)).thenReturn(0L);
        when(repositoryPort.findFirstDrawDateByType(any())).thenReturn(Optional.empty());
        when(repositoryPort.findLastDrawDateByType(any())).thenReturn(Optional.empty());

        LotteryStatistics stats = service.getStatistics(LotteryType.MELATE);

        assertThat(stats.getAverageFrequency()).isEqualTo(0.0);
        assertThat(stats.getMostFrequent()).isEmpty();
    }
}
