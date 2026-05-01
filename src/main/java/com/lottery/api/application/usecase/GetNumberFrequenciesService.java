package com.lottery.api.application.usecase;

import com.lottery.api.domain.exception.LotteryException;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.NumberFrequency;
import com.lottery.api.domain.port.in.GetNumberFrequenciesUseCase;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Servicio que expone la frecuencia histórica de cada número del rango del juego.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetNumberFrequenciesService implements GetNumberFrequenciesUseCase {

    private final LotteryDrawRepositoryPort repositoryPort;

    @Override
    @Transactional(readOnly = true)
    public List<NumberFrequency> getNumberFrequencies(LotteryType lotteryType) {
        log.debug("Consultando frecuencias para: {}", lotteryType);
        return repositoryPort.getNumberFrequencies(lotteryType).stream()
                .sorted(Comparator.comparingInt(NumberFrequency::getNumber))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NumberFrequency getNumberFrequency(LotteryType lotteryType, int number) {
        validateNumber(lotteryType, number);
        return repositoryPort.getNumberFrequencies(lotteryType).stream()
                .filter(nf -> nf.getNumber().equals(number))
                .findFirst()
                .orElse(NumberFrequency.builder()
                        .number(number)
                        .frequency(0L)
                        .percentage(0.0)
                        .build());
    }

    private void validateNumber(LotteryType type, int number) {
        if (number < type.getMinNumber() || number > type.getMaxNumber()) {
            throw new LotteryException(
                    String.format("El número %d está fuera del rango válido [%d-%d] para %s",
                            number, type.getMinNumber(), type.getMaxNumber(), type.getDisplayName()));
        }
    }
}
