package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.LotteryDraw;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetHotNumbersService — Tests Unitarios")
class GetHotNumbersServiceTest {

    @Mock private LotteryDrawRepositoryPort repositoryPort;
    @InjectMocks private GetHotNumbersService service;

    private List<NumberFrequency> buildFrequencies() {
        return List.of(
                NumberFrequency.builder().number(7).frequency(300L).build(),
                NumberFrequency.builder().number(13).frequency(100L).build(),
                NumberFrequency.builder().number(23).frequency(250L).build(),
                NumberFrequency.builder().number(42).frequency(50L).build(),
                NumberFrequency.builder().number(56).frequency(200L).build()
        );
    }

    @Test
    @DisplayName("debe devolver números calientes ordenados de mayor a menor frecuencia")
    void getHotNumbers_returnsTopByFrequencyDesc() {
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(buildFrequencies());

        List<NumberFrequency> hot = service.getHotNumbers(LotteryType.MELATE, 3);

        assertThat(hot).hasSize(3);
        assertThat(hot.get(0).getNumber()).isEqualTo(7);   // 300
        assertThat(hot.get(1).getNumber()).isEqualTo(23);  // 250
        assertThat(hot.get(2).getNumber()).isEqualTo(56);  // 200
    }

    @Test
    @DisplayName("debe devolver números fríos ordenados de menor a mayor frecuencia")
    void getColdNumbers_returnsBottomByFrequencyAsc() {
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(buildFrequencies());

        List<NumberFrequency> cold = service.getColdNumbers(LotteryType.MELATE, 2);

        assertThat(cold).hasSize(2);
        assertThat(cold.get(0).getNumber()).isEqualTo(42);  // 50
        assertThat(cold.get(1).getNumber()).isEqualTo(13);  // 100
    }

    @Test
    @DisplayName("límite mayor que total devuelve todos los disponibles")
    void getHotNumbers_limitGreaterThanTotal_returnsAll() {
        when(repositoryPort.getNumberFrequencies(LotteryType.REVANCHA)).thenReturn(buildFrequencies());

        List<NumberFrequency> hot = service.getHotNumbers(LotteryType.REVANCHA, 100);

        assertThat(hot).hasSize(5);
    }

    @Test
    @DisplayName("getRecentHotNumbers debe calcular frecuencias de los últimos N sorteos")
    void getRecentHotNumbers_calculatesFromRecentDraws() {
        List<LotteryDraw> recentDraws = List.of(
                LotteryDraw.builder().lotteryType(LotteryType.MELATE)
                        .drawNumber(10).drawDate(LocalDate.now())
                        .numbers(List.of(1, 2, 3, 4, 5, 1)).build(), // 1 aparece 2 veces
                LotteryDraw.builder().lotteryType(LotteryType.MELATE)
                        .drawNumber(9).drawDate(LocalDate.now().minusDays(3))
                        .numbers(List.of(1, 6, 7, 8, 9, 10)).build() // 1 aparece 1 vez más
        );
        when(repositoryPort.findRecentByType(LotteryType.MELATE, 2)).thenReturn(recentDraws);

        List<NumberFrequency> result = service.getRecentHotNumbers(LotteryType.MELATE, 2, 3);

        assertThat(result).isNotEmpty();
        // número 1 debería ser el más frecuente (aparece 3 veces en total)
        assertThat(result.get(0).getNumber()).isEqualTo(1);
        assertThat(result.get(0).getFrequency()).isEqualTo(3L);
    }

    @Test
    @DisplayName("getRecentHotNumbers con lista vacía devuelve lista vacía")
    void getRecentHotNumbers_emptyDraws_returnsEmpty() {
        when(repositoryPort.findRecentByType(LotteryType.MELATE, 5)).thenReturn(List.of());

        List<NumberFrequency> result = service.getRecentHotNumbers(LotteryType.MELATE, 5, 3);

        assertThat(result).isEmpty();
    }
}
