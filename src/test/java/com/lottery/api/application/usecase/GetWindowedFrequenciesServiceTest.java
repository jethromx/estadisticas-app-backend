package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.NumberFrequency;
import com.lottery.api.domain.model.WindowedFrequency;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetWindowedFrequenciesService — Tests Unitarios")
class GetWindowedFrequenciesServiceTest {

    @Mock private LotteryDrawRepositoryPort repositoryPort;

    @InjectMocks private GetWindowedFrequenciesService service;

    @Test
    @DisplayName("debe retornar lista vacía cuando no hay sorteos")
    void getWindowedFrequencies_noDraws_emptyList() {
        when(repositoryPort.countByType(LotteryType.MELATE)).thenReturn(0L);

        List<WindowedFrequency> result = service.getWindowedFrequencies(LotteryType.MELATE, 10);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("debe retornar lista vacía cuando no hay sorteos en la ventana")
    void getWindowedFrequencies_noDrawsInWindow_emptyList() {
        when(repositoryPort.countByType(LotteryType.MELATE)).thenReturn(100L);
        when(repositoryPort.countDrawsInWindow(LotteryType.MELATE, 5)).thenReturn(0L);

        List<WindowedFrequency> result = service.getWindowedFrequencies(LotteryType.MELATE, 5);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("debe calcular trend positivo cuando la tasa reciente supera la histórica")
    void getWindowedFrequencies_higherRecentRate_positiveTrend() {
        // Histórico: número 1 aparece 10 veces en 100 sorteos → tasa 0.10
        List<NumberFrequency> historical = List.of(
                NumberFrequency.builder().number(1).frequency(10).percentage(10.0).build()
        );
        // Ventana: número 1 aparece 8 veces en 20 sorteos → tasa 0.40 (mucho mayor que 0.10)
        List<NumberFrequency> windowed = List.of(
                NumberFrequency.builder().number(1).frequency(8).percentage(40.0).build()
        );

        when(repositoryPort.countByType(LotteryType.MELATE)).thenReturn(100L);
        when(repositoryPort.countDrawsInWindow(LotteryType.MELATE, 20)).thenReturn(20L);
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(historical);
        when(repositoryPort.getFrequenciesByDrawWindow(LotteryType.MELATE, 20)).thenReturn(windowed);

        List<WindowedFrequency> result = service.getWindowedFrequencies(LotteryType.MELATE, 20);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTrend()).isPositive();
    }

    @Test
    @DisplayName("debe calcular trend negativo cuando la tasa reciente es menor que la histórica")
    void getWindowedFrequencies_lowerRecentRate_negativeTrend() {
        // Histórico: número 5 aparece 50 veces en 100 sorteos → tasa 0.50
        List<NumberFrequency> historical = List.of(
                NumberFrequency.builder().number(5).frequency(50).percentage(50.0).build()
        );
        // Ventana: número 5 aparece 2 veces en 20 sorteos → tasa 0.10 (menor que 0.50)
        List<NumberFrequency> windowed = List.of(
                NumberFrequency.builder().number(5).frequency(2).percentage(10.0).build()
        );

        when(repositoryPort.countByType(LotteryType.MELATE)).thenReturn(100L);
        when(repositoryPort.countDrawsInWindow(LotteryType.MELATE, 20)).thenReturn(20L);
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(historical);
        when(repositoryPort.getFrequenciesByDrawWindow(LotteryType.MELATE, 20)).thenReturn(windowed);

        List<WindowedFrequency> result = service.getWindowedFrequencies(LotteryType.MELATE, 20);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTrend()).isNegative();
    }

    @Test
    @DisplayName("debe calcular trend = 0.0 cuando las tasas histórica y reciente son iguales")
    void getWindowedFrequencies_equalRates_zeroTrend() {
        // Histórico: número 3 aparece 20 veces en 100 sorteos → tasa 0.20
        List<NumberFrequency> historical = List.of(
                NumberFrequency.builder().number(3).frequency(20).percentage(20.0).build()
        );
        // Ventana: número 3 aparece 4 veces en 20 sorteos → tasa 0.20 (igual)
        List<NumberFrequency> windowed = List.of(
                NumberFrequency.builder().number(3).frequency(4).percentage(20.0).build()
        );

        when(repositoryPort.countByType(LotteryType.MELATE)).thenReturn(100L);
        when(repositoryPort.countDrawsInWindow(LotteryType.MELATE, 20)).thenReturn(20L);
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(historical);
        when(repositoryPort.getFrequenciesByDrawWindow(LotteryType.MELATE, 20)).thenReturn(windowed);

        List<WindowedFrequency> result = service.getWindowedFrequencies(LotteryType.MELATE, 20);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTrend()).isCloseTo(0.0, within(0.01));
    }

    @Test
    @DisplayName("debe ordenar resultados por número ascendente")
    void getWindowedFrequencies_results_sortedByNumberAsc() {
        List<NumberFrequency> historical = List.of(
                NumberFrequency.builder().number(10).frequency(10).percentage(10.0).build(),
                NumberFrequency.builder().number(5).frequency(10).percentage(10.0).build(),
                NumberFrequency.builder().number(1).frequency(10).percentage(10.0).build()
        );
        List<NumberFrequency> windowed = List.of(
                NumberFrequency.builder().number(10).frequency(2).percentage(20.0).build(),
                NumberFrequency.builder().number(5).frequency(2).percentage(20.0).build(),
                NumberFrequency.builder().number(1).frequency(2).percentage(20.0).build()
        );

        when(repositoryPort.countByType(LotteryType.MELATE)).thenReturn(100L);
        when(repositoryPort.countDrawsInWindow(LotteryType.MELATE, 10)).thenReturn(10L);
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(historical);
        when(repositoryPort.getFrequenciesByDrawWindow(LotteryType.MELATE, 10)).thenReturn(windowed);

        List<WindowedFrequency> result = service.getWindowedFrequencies(LotteryType.MELATE, 10);

        assertThat(result).extracting(WindowedFrequency::getNumber).containsExactly(1, 5, 10);
    }

    @Test
    @DisplayName("debe preservar windowSize y windowDrawCount en los resultados")
    void getWindowedFrequencies_result_preservesWindowMetadata() {
        List<NumberFrequency> historical = List.of(
                NumberFrequency.builder().number(7).frequency(15).percentage(15.0).build()
        );
        List<NumberFrequency> windowed = List.of(
                NumberFrequency.builder().number(7).frequency(3).percentage(30.0).build()
        );

        when(repositoryPort.countByType(LotteryType.REVANCHA)).thenReturn(200L);
        when(repositoryPort.countDrawsInWindow(LotteryType.REVANCHA, 25)).thenReturn(25L);
        when(repositoryPort.getNumberFrequencies(LotteryType.REVANCHA)).thenReturn(historical);
        when(repositoryPort.getFrequenciesByDrawWindow(LotteryType.REVANCHA, 25)).thenReturn(windowed);

        List<WindowedFrequency> result = service.getWindowedFrequencies(LotteryType.REVANCHA, 25);

        assertThat(result).hasSize(1);
        WindowedFrequency wf = result.get(0);
        assertThat(wf.getWindowSize()).isEqualTo(25);
        assertThat(wf.getWindowDrawCount()).isEqualTo(25);
    }

    @Test
    @DisplayName("debe calcular trend en puntos porcentuales redondeados a 2 decimales")
    void getWindowedFrequencies_trend_roundedToTwoDecimals() {
        // histórico: 15/100 = 0.15; ventana: 6/20 = 0.30
        // trend = (0.30 - 0.15) / 0.15 * 100 = 100.0 %
        List<NumberFrequency> historical = List.of(
                NumberFrequency.builder().number(2).frequency(15).percentage(15.0).build()
        );
        List<NumberFrequency> windowed = List.of(
                NumberFrequency.builder().number(2).frequency(6).percentage(30.0).build()
        );

        when(repositoryPort.countByType(LotteryType.MELATE)).thenReturn(100L);
        when(repositoryPort.countDrawsInWindow(LotteryType.MELATE, 20)).thenReturn(20L);
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(historical);
        when(repositoryPort.getFrequenciesByDrawWindow(LotteryType.MELATE, 20)).thenReturn(windowed);

        List<WindowedFrequency> result = service.getWindowedFrequencies(LotteryType.MELATE, 20);

        assertThat(result.get(0).getTrend()).isCloseTo(100.0, within(0.01));
    }
}
