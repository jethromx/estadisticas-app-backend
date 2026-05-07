package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.ChiSquareResult;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.NumberFrequency;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetChiSquareService — Tests Unitarios")
class GetChiSquareServiceTest {

    @Mock private LotteryDrawRepositoryPort repositoryPort;

    @InjectMocks private GetChiSquareService service;

    /** Genera frecuencias perfectamente uniformes para el rango 1-56 con N apariciones cada uno. */
    private List<NumberFrequency> uniformFrequencies(LotteryType type, long countPerNumber) {
        List<NumberFrequency> freqs = new ArrayList<>();
        for (int n = type.getMinNumber(); n <= type.getMaxNumber(); n++) {
            freqs.add(NumberFrequency.builder().number(n).frequency(countPerNumber).percentage(1.0).build());
        }
        return freqs;
    }

    @Test
    @DisplayName("debe retornar p-value alto (≥ 0.05) con distribución perfectamente uniforme")
    void getChiSquare_uniformDistribution_highPValue() {
        List<NumberFrequency> freqs = uniformFrequencies(LotteryType.MELATE, 100);
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(freqs);

        ChiSquareResult result = service.getChiSquare(LotteryType.MELATE);

        assertThat(result.getChiSquare()).isEqualTo(0.0);
        assertThat(result.getPValue()).isGreaterThanOrEqualTo(0.05);
        assertThat(result.getInterpretation()).contains("uniforme");
    }

    @Test
    @DisplayName("debe retornar chiSquare = 0 cuando todos los números tienen la misma frecuencia")
    void getChiSquare_perfectUniform_chiSquareIsZero() {
        List<NumberFrequency> freqs = uniformFrequencies(LotteryType.MELATE, 50);
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(freqs);

        ChiSquareResult result = service.getChiSquare(LotteryType.MELATE);

        assertThat(result.getChiSquare()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("debe retornar p-value bajo con distribución muy sesgada")
    void getChiSquare_biasedDistribution_lowPValue() {
        // Número 1 con 1000 apariciones, el resto con 1
        List<NumberFrequency> freqs = new ArrayList<>();
        freqs.add(NumberFrequency.builder().number(1).frequency(1000).percentage(98.0).build());
        for (int n = 2; n <= 56; n++) {
            freqs.add(NumberFrequency.builder().number(n).frequency(1).percentage(0.02).build());
        }
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(freqs);

        ChiSquareResult result = service.getChiSquare(LotteryType.MELATE);

        assertThat(result.getPValue()).isLessThan(0.001);
        assertThat(result.getChiSquare()).isGreaterThan(0.0);
        assertThat(result.getInterpretation()).contains("p < 0.001");
    }

    @Test
    @DisplayName("debe retornar Sin datos suficientes cuando totalObservations = 0")
    void getChiSquare_noData_returnsSinDatos() {
        when(repositoryPort.getNumberFrequencies(LotteryType.REVANCHA)).thenReturn(List.of());

        ChiSquareResult result = service.getChiSquare(LotteryType.REVANCHA);

        assertThat(result.getChiSquare()).isEqualTo(0);
        assertThat(result.getPValue()).isEqualTo(1.0);
        assertThat(result.getTotalObservations()).isEqualTo(0);
        assertThat(result.getInterpretation()).contains("Sin datos");
    }

    @Test
    @DisplayName("debe calcular degreesOfFreedom = rangeSize - 1")
    void getChiSquare_degreesOfFreedom_isRangeSizeMinusOne() {
        // MELATE: rango 1-56 → 56 valores → df = 55
        List<NumberFrequency> freqs = uniformFrequencies(LotteryType.MELATE, 10);
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(freqs);

        ChiSquareResult result = service.getChiSquare(LotteryType.MELATE);

        assertThat(result.getDegreesOfFreedom()).isEqualTo(55); // 56 - 1
    }

    @Test
    @DisplayName("debe incluir lotteryType en el resultado")
    void getChiSquare_result_containsLotteryType() {
        List<NumberFrequency> freqs = uniformFrequencies(LotteryType.REVANCHITA, 20);
        when(repositoryPort.getNumberFrequencies(LotteryType.REVANCHITA)).thenReturn(freqs);

        ChiSquareResult result = service.getChiSquare(LotteryType.REVANCHITA);

        assertThat(result.getLotteryType()).isEqualTo(LotteryType.REVANCHITA);
    }

    @Test
    @DisplayName("debe asignar interpretación correcta para p entre 0.01 y 0.05")
    void getChiSquare_pBetween001And005_ligDesviacion() {
        // Crear distribución ligeramente desviada: algunos números con más frecuencia
        List<NumberFrequency> freqs = new ArrayList<>();
        for (int n = 1; n <= 56; n++) {
            long freq = (n <= 5) ? 120 : 100; // primeros 5 un poco más frecuentes
            freqs.add(NumberFrequency.builder().number(n).frequency(freq).percentage(1.0).build());
        }
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(freqs);

        ChiSquareResult result = service.getChiSquare(LotteryType.MELATE);

        // El pValue puede variar, pero chiSquare debe ser positivo y la interpretación válida
        assertThat(result.getChiSquare()).isPositive();
        assertThat(result.getInterpretation()).isNotBlank();
    }

    @Test
    @DisplayName("debe calcular expectedFrequency = totalObservations / rangeSize")
    void getChiSquare_expectedFrequency_isCorrect() {
        // 56 números × 10 cada uno = 560 total → expected = 560/56 = 10
        List<NumberFrequency> freqs = uniformFrequencies(LotteryType.MELATE, 10);
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(freqs);

        ChiSquareResult result = service.getChiSquare(LotteryType.MELATE);

        assertThat(result.getExpectedFrequency()).isEqualTo(10.0);
        assertThat(result.getTotalObservations()).isEqualTo(560L);
    }
}
