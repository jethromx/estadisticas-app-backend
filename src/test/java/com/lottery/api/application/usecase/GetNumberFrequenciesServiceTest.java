package com.lottery.api.application.usecase;

import com.lottery.api.domain.exception.LotteryException;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.NumberFrequency;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetNumberFrequenciesService — Tests Unitarios")
class GetNumberFrequenciesServiceTest {

    @Mock private LotteryDrawRepositoryPort repositoryPort;
    @InjectMocks private GetNumberFrequenciesService service;

    private List<NumberFrequency> sampleFrequencies() {
        return List.of(
                NumberFrequency.builder().number(10).frequency(200L).percentage(5.0).build(),
                NumberFrequency.builder().number(5).frequency(150L).percentage(3.75).build(),
                NumberFrequency.builder().number(30).frequency(180L).percentage(4.5).build()
        );
    }

    @Test
    @DisplayName("debe devolver frecuencias ordenadas por número ascendente")
    void getNumberFrequencies_returnsSortedByNumber() {
        when(repositoryPort.getNumberFrequencies(LotteryType.REVANCHA)).thenReturn(sampleFrequencies());

        List<NumberFrequency> result = service.getNumberFrequencies(LotteryType.REVANCHA);

        assertThat(result).extracting(NumberFrequency::getNumber)
                .containsExactly(5, 10, 30);
    }

    @Test
    @DisplayName("debe devolver frecuencia de número específico cuando existe")
    void getNumberFrequency_existingNumber_returnsIt() {
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(sampleFrequencies());

        NumberFrequency result = service.getNumberFrequency(LotteryType.MELATE, 10);

        assertThat(result.getNumber()).isEqualTo(10);
        assertThat(result.getFrequency()).isEqualTo(200L);
    }

    @Test
    @DisplayName("debe devolver frecuencia 0 para número nunca sorteado")
    void getNumberFrequency_neverDrawnNumber_returnsZeroFrequency() {
        when(repositoryPort.getNumberFrequencies(LotteryType.MELATE)).thenReturn(sampleFrequencies());

        NumberFrequency result = service.getNumberFrequency(LotteryType.MELATE, 56);

        assertThat(result.getNumber()).isEqualTo(56);
        assertThat(result.getFrequency()).isEqualTo(0L);
    }

    @Test
    @DisplayName("debe lanzar LotteryException para número fuera de rango")
    void getNumberFrequency_outOfRange_throwsLotteryException() {
        // Melate rango 1-56
        assertThatThrownBy(() -> service.getNumberFrequency(LotteryType.MELATE, 0))
                .isInstanceOf(LotteryException.class)
                .hasMessageContaining("fuera del rango");

        assertThatThrownBy(() -> service.getNumberFrequency(LotteryType.MELATE, 57))
                .isInstanceOf(LotteryException.class);
    }

}
