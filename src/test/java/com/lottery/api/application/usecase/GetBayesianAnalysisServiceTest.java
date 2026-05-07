package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.BayesianNumber;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetBayesianAnalysisService — Tests Unitarios")
class GetBayesianAnalysisServiceTest {

    @Mock private LotteryDrawRepositoryPort repositoryPort;

    @InjectMocks private GetBayesianAnalysisService service;

    private NumberFrequency freq1;
    private NumberFrequency freq2;
    private NumberFrequency freq3;

    @BeforeEach
    void setUp() {
        freq1 = NumberFrequency.builder().number(1).frequency(100).percentage(10.0).build();
        freq2 = NumberFrequency.builder().number(2).frequency(50).percentage(5.0).build();
        freq3 = NumberFrequency.builder().number(3).frequency(150).percentage(15.0).build();
    }

    @Test
    @DisplayName("debe calcular posteriorMean mayor para números con alta frecuencia reciente")
    void getBayesianAnalysis_highRecentFrequency_higherPosterior() {
        List<NumberFrequency> historical = List.of(freq1, freq2);
        // Número 1 aparece mucho recientemente, número 2 nada
        List<NumberFrequency> recent = List.of(
                NumberFrequency.builder().number(1).frequency(8).percentage(80.0).build()
        );

        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(historical);
        when(repositoryPort.getFrequenciesByDrawWindow(LotteryType.MELATE, 10)).thenReturn(recent);

        List<BayesianNumber> result = service.getBayesianAnalysis(LotteryType.MELATE, 10);

        assertThat(result).hasSize(2);
        BayesianNumber first = result.get(0);
        BayesianNumber second = result.get(1);

        // Ordenados por posteriorMean descendente
        assertThat(first.getPosteriorMean()).isGreaterThan(second.getPosteriorMean());
        // El número 1 (alta frecuencia reciente) debe ser el primero
        assertThat(first.getNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("debe calcular lift positivo cuando tasa reciente supera la tasa histórica")
    void getBayesianAnalysis_recentRateAbovePrior_positiveLift() {
        // Número 1 tiene frecuencia histórica muy baja (1 de 100) → priorMean ≈ 0.02
        // En la ventana reciente aparece 2 veces en 10 sorteos × 7 nums = 70 slots
        // windowRate = 2/70 ≈ 0.029 > 0.02 → posteriorMean > priorMean → lift positivo
        NumberFrequency lowFreqHist = NumberFrequency.builder().number(1).frequency(1).percentage(1.0).build();
        NumberFrequency highFreqHist = NumberFrequency.builder().number(2).frequency(99).percentage(99.0).build();
        List<NumberFrequency> historical = List.of(lowFreqHist, highFreqHist);

        List<NumberFrequency> recent = List.of(
                NumberFrequency.builder().number(1).frequency(2).percentage(100.0).build()
        );

        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(historical);
        when(repositoryPort.getFrequenciesByDrawWindow(LotteryType.MELATE, 10)).thenReturn(recent);

        List<BayesianNumber> result = service.getBayesianAnalysis(LotteryType.MELATE, 10);

        BayesianNumber number1 = result.stream().filter(b -> b.getNumber() == 1).findFirst().orElseThrow();
        assertThat(number1.getLift()).isPositive();
    }

    @Test
    @DisplayName("debe calcular lift negativo cuando número no aparece en ventana reciente")
    void getBayesianAnalysis_zeroRecentFrequency_negativeLift() {
        List<NumberFrequency> historical = List.of(freq1);
        // Número 1 no aparece en la ventana reciente
        List<NumberFrequency> recent = List.of();

        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(historical);
        when(repositoryPort.getFrequenciesByDrawWindow(LotteryType.MELATE, 20)).thenReturn(recent);

        List<BayesianNumber> result = service.getBayesianAnalysis(LotteryType.MELATE, 20);

        assertThat(result).hasSize(1);
        BayesianNumber bn = result.get(0);
        // Con 0 apariciones recientes el posterior cae bajo el prior → lift negativo
        assertThat(bn.getLift()).isNegative();
        assertThat(bn.getRecentFrequency()).isEqualTo(0L);
    }

    @Test
    @DisplayName("debe retornar lista vacía cuando no hay frecuencias históricas")
    void getBayesianAnalysis_noHistoricalData_emptyList() {
        when(repositoryPort.getNumberFrequencies(LotteryType.REVANCHA)).thenReturn(List.of());
        when(repositoryPort.getFrequenciesByDrawWindow(LotteryType.REVANCHA, 10)).thenReturn(List.of());

        List<BayesianNumber> result = service.getBayesianAnalysis(LotteryType.REVANCHA, 10);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("debe redondear posteriorMean a 6 decimales")
    void getBayesianAnalysis_result_roundedToSixDecimals() {
        List<NumberFrequency> historical = List.of(freq1);
        List<NumberFrequency> recent = List.of(
                NumberFrequency.builder().number(1).frequency(3).percentage(30.0).build()
        );

        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(historical);
        when(repositoryPort.getFrequenciesByDrawWindow(LotteryType.MELATE, 10)).thenReturn(recent);

        List<BayesianNumber> result = service.getBayesianAnalysis(LotteryType.MELATE, 10);

        BayesianNumber bn = result.get(0);
        // Verificar que el valor no tiene más de 6 decimales
        String posteriorStr = Double.toString(bn.getPosteriorMean());
        int decimalIndex = posteriorStr.indexOf('.');
        if (decimalIndex >= 0) {
            assertThat(posteriorStr.length() - decimalIndex - 1).isLessThanOrEqualTo(6);
        }
    }

    @Test
    @DisplayName("debe preservar recentWindow en los resultados")
    void getBayesianAnalysis_result_preservesWindow() {
        List<NumberFrequency> historical = List.of(freq1);
        List<NumberFrequency> recent = List.of();

        when(repositoryPort.getNumberFrequencies(LotteryType.REVANCHITA)).thenReturn(historical);
        when(repositoryPort.getFrequenciesByDrawWindow(LotteryType.REVANCHITA, 30)).thenReturn(recent);

        List<BayesianNumber> result = service.getBayesianAnalysis(LotteryType.REVANCHITA, 30);

        assertThat(result).allMatch(bn -> bn.getRecentWindow() == 30);
    }

    @Test
    @DisplayName("debe ordenar resultados por posteriorMean descendente")
    void getBayesianAnalysis_results_sortedByPosteriorMeanDesc() {
        List<NumberFrequency> historical = List.of(freq1, freq2, freq3);
        // Números 3 con alta frecuencia reciente, número 2 con nada
        List<NumberFrequency> recent = List.of(
                NumberFrequency.builder().number(3).frequency(6).percentage(60.0).build(),
                NumberFrequency.builder().number(1).frequency(2).percentage(20.0).build()
        );

        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(historical);
        when(repositoryPort.getFrequenciesByDrawWindow(LotteryType.MELATE, 10)).thenReturn(recent);

        List<BayesianNumber> result = service.getBayesianAnalysis(LotteryType.MELATE, 10);

        for (int i = 0; i < result.size() - 1; i++) {
            assertThat(result.get(i).getPosteriorMean())
                    .isGreaterThanOrEqualTo(result.get(i + 1).getPosteriorMean());
        }
    }
}
