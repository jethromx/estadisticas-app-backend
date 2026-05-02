package com.lottery.api.application.usecase;

import com.lottery.api.application.util.StatUtils;
import com.lottery.api.domain.model.ChiSquareResult;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.NumberFrequency;
import com.lottery.api.domain.port.in.GetChiSquareUseCase;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetChiSquareService implements GetChiSquareUseCase {

    private final LotteryDrawRepositoryPort repositoryPort;

    @Override
    @Transactional(readOnly = true)
    public ChiSquareResult getChiSquare(LotteryType type) {
        List<NumberFrequency> freqs = repositoryPort.getNumberFrequencies(type);

        int rangeSize = type.getMaxNumber() - type.getMinNumber() + 1;
        long totalObservations = freqs.stream().mapToLong(NumberFrequency::getFrequency).sum();

        if (totalObservations == 0) {
            return ChiSquareResult.builder()
                    .lotteryType(type).chiSquare(0).degreesOfFreedom(rangeSize - 1)
                    .pValue(1.0).totalObservations(0).expectedFrequency(0)
                    .interpretation("Sin datos suficientes.").build();
        }

        double expected = (double) totalObservations / rangeSize;

        Map<Integer, Long> freqMap = freqs.stream()
                .collect(Collectors.toMap(NumberFrequency::getNumber, NumberFrequency::getFrequency));

        double chiSquare = 0;
        for (int n = type.getMinNumber(); n <= type.getMaxNumber(); n++) {
            long observed = freqMap.getOrDefault(n, 0L);
            chiSquare += Math.pow(observed - expected, 2) / expected;
        }

        int df = rangeSize - 1;
        double pValue = StatUtils.chiSquarePValue(chiSquare, df);

        String interpretation;
        if (pValue < 0.001)      interpretation = "Distribución muy significativamente no uniforme (p < 0.001) — hay números favorecidos estadísticamente";
        else if (pValue < 0.01)  interpretation = "Distribución significativamente no uniforme (p < 0.01)";
        else if (pValue < 0.05)  interpretation = "Ligera desviación de la uniformidad (p < 0.05)";
        else                     interpretation = "Distribución estadísticamente uniforme (p ≥ 0.05) — no hay evidencia de sesgo";

        return ChiSquareResult.builder()
                .lotteryType(type)
                .chiSquare(Math.round(chiSquare * 100.0) / 100.0)
                .degreesOfFreedom(df)
                .pValue(Math.round(pValue * 100000.0) / 100000.0)
                .totalObservations(totalObservations)
                .expectedFrequency(Math.round(expected * 100.0) / 100.0)
                .interpretation(interpretation)
                .build();
    }
}
