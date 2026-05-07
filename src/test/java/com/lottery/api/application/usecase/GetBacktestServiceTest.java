package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.BacktestResult;
import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.NumberFrequency;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetBacktestService — Tests Unitarios")
class GetBacktestServiceTest {

    @Mock private LotteryDrawRepositoryPort repositoryPort;

    @InjectMocks private GetBacktestService service;

    private List<NumberFrequency> frequencies;

    @BeforeEach
    void setUp() {
        // 6 números ordenados por frecuencia descendente: 1(100), 2(90), 3(80), 4(70), 5(60), 6(50)
        frequencies = List.of(
                NumberFrequency.builder().number(1).frequency(100).percentage(10.0).build(),
                NumberFrequency.builder().number(2).frequency(90).percentage(9.0).build(),
                NumberFrequency.builder().number(3).frequency(80).percentage(8.0).build(),
                NumberFrequency.builder().number(4).frequency(70).percentage(7.0).build(),
                NumberFrequency.builder().number(5).frequency(60).percentage(6.0).build(),
                NumberFrequency.builder().number(6).frequency(50).percentage(5.0).build()
        );
    }

    private LotteryDraw drawWith(List<Integer> numbers) {
        return LotteryDraw.builder()
                .lotteryType(LotteryType.MELATE)
                .drawNumber(1000)
                .drawDate(LocalDate.of(2026, 1, 1))
                .numbers(numbers)
                .build();
    }

    @Test
    @DisplayName("debe seleccionar los topK números más frecuentes como predicciones")
    void getBacktest_selectsTopKByFrequency() {
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(frequencies);
        when(repositoryPort.findRecentByType(LotteryType.MELATE, 20)).thenReturn(List.of());

        BacktestResult result = service.getBacktest(LotteryType.MELATE, 6, 20);

        assertThat(result.getPredictedNumbers()).containsExactly(1, 2, 3, 4, 5, 6);
        assertThat(result.getTopK()).isEqualTo(6);
    }

    @Test
    @DisplayName("debe calcular hitRate = 1.0 cuando todos los sorteos tienen al menos 1 coincidencia")
    void getBacktest_allDrawsMatch_hitRateIsOne() {
        LotteryDraw draw1 = drawWith(List.of(1, 7, 8, 9, 10, 11));  // número 1 coincide
        LotteryDraw draw2 = drawWith(List.of(2, 12, 13, 14, 15, 16)); // número 2 coincide

        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(frequencies);
        when(repositoryPort.findRecentByType(LotteryType.MELATE, 20)).thenReturn(List.of(draw1, draw2));

        BacktestResult result = service.getBacktest(LotteryType.MELATE, 6, 20);

        assertThat(result.getHitRate()).isEqualTo(1.0);
        assertThat(result.getTotalDrawsTested()).isEqualTo(2);
    }

    @Test
    @DisplayName("debe calcular hitRate = 0.0 cuando ningún sorteo tiene coincidencias")
    void getBacktest_noDrawsMatch_hitRateIsZero() {
        // Sorteos sin números 1-6
        LotteryDraw draw1 = drawWith(List.of(20, 30, 40, 50, 51, 52));
        LotteryDraw draw2 = drawWith(List.of(21, 31, 41, 46, 47, 48));

        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(frequencies);
        when(repositoryPort.findRecentByType(LotteryType.MELATE, 20)).thenReturn(List.of(draw1, draw2));

        BacktestResult result = service.getBacktest(LotteryType.MELATE, 6, 20);

        assertThat(result.getHitRate()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("debe calcular avgMatches correctamente")
    void getBacktest_calculatesAvgMatches() {
        // draw1 tiene 2 coincidencias (1, 2), draw2 tiene 1 (3)
        LotteryDraw draw1 = drawWith(List.of(1, 2, 20, 21, 22, 23));
        LotteryDraw draw2 = drawWith(List.of(3, 20, 21, 22, 23, 24));

        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(frequencies);
        when(repositoryPort.findRecentByType(LotteryType.MELATE, 20)).thenReturn(List.of(draw1, draw2));

        BacktestResult result = service.getBacktest(LotteryType.MELATE, 6, 20);

        // (2 + 1) / 2 = 1.5
        assertThat(result.getAvgMatches()).isEqualTo(1.5);
    }

    @Test
    @DisplayName("debe retornar avgMatches=0 y hitRate=0 cuando no hay sorteos de prueba")
    void getBacktest_noTestDraws_zeroMetrics() {
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(frequencies);
        when(repositoryPort.findRecentByType(LotteryType.MELATE, 20)).thenReturn(List.of());

        BacktestResult result = service.getBacktest(LotteryType.MELATE, 6, 20);

        assertThat(result.getAvgMatches()).isEqualTo(0.0);
        assertThat(result.getHitRate()).isEqualTo(0.0);
        assertThat(result.getTotalDrawsTested()).isEqualTo(0);
    }

    @Test
    @DisplayName("debe inicializar matchDistribution con claves de 0 a topK")
    void getBacktest_matchDistribution_hasAllKeys() {
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(frequencies);
        when(repositoryPort.findRecentByType(LotteryType.MELATE, 20)).thenReturn(List.of());

        BacktestResult result = service.getBacktest(LotteryType.MELATE, 6, 20);

        for (int i = 0; i <= 6; i++) {
            assertThat(result.getMatchDistribution()).containsKey(i);
        }
    }

    @Test
    @DisplayName("debe calcular expectedRandomRate mayor que cero para parámetros válidos")
    void getBacktest_expectedRandomRate_isPositive() {
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(frequencies);
        when(repositoryPort.findRecentByType(LotteryType.MELATE, 20)).thenReturn(List.of());

        BacktestResult result = service.getBacktest(LotteryType.MELATE, 6, 20);

        // Melate: rango 1-56, topK=6, numbersCount=6 → probabilidad hipergeométrica > 0
        assertThat(result.getExpectedRandomRate()).isGreaterThan(0.0).isLessThanOrEqualTo(1.0);
    }

    @Test
    @DisplayName("debe retornar lotteryType y strategy en el resultado")
    void getBacktest_result_containsMetadata() {
        when(repositoryPort.getNumberFrequencies(LotteryType.REVANCHA)).thenReturn(List.of());
        when(repositoryPort.findRecentByType(LotteryType.REVANCHA, 10)).thenReturn(List.of());

        BacktestResult result = service.getBacktest(LotteryType.REVANCHA, 3, 10);

        assertThat(result.getLotteryType()).isEqualTo(LotteryType.REVANCHA);
        assertThat(result.getStrategy()).isEqualTo("HOT_NUMBERS");
    }
}
