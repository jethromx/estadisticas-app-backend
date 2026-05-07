package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.EntropyAnalysis;
import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.WindowEntropy;
import com.lottery.api.domain.port.in.GetEntropyAnalysisUseCase;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GetEntropyAnalysisService implements GetEntropyAnalysisUseCase {

    private final LotteryDrawRepositoryPort repositoryPort;

    @Override
    @Cacheable(value = "analysis-entropy", key = "#type.name() + '-' + #windowSize")
    @Transactional(readOnly = true)
    public EntropyAnalysis getEntropyAnalysis(LotteryType type, int windowSize) {
        List<LotteryDraw> draws = repositoryPort.findByType(type);
        // Oldest-first for windowed analysis
        List<LotteryDraw> ordered = new ArrayList<>(draws);
        java.util.Collections.reverse(ordered);

        int totalDraws = ordered.size();
        int distinctNumbers = type.getMaxNumber() - type.getMinNumber() + 1;
        double maxEntropy = Math.log(distinctNumbers) / Math.log(2);

        // Global frequency
        Map<Integer, Long> globalFreq = new HashMap<>();
        for (LotteryDraw d : ordered) {
            if (d.getNumbers() == null) continue;
            for (int n : d.getNumbers()) globalFreq.merge(n, 1L, (a, b) -> a + b);
        }
        double observedEntropy = computeEntropy(globalFreq);
        double entropyRatio = maxEntropy > 0 ? Math.round(observedEntropy / maxEntropy * 10000.0) / 10000.0 : 0;

        // Rolling windows
        List<WindowEntropy> windows = new ArrayList<>();
        int numWindows = totalDraws / windowSize;
        for (int w = 0; w < numWindows; w++) {
            int from = w * windowSize;
            int to = Math.min(from + windowSize, totalDraws);
            List<LotteryDraw> slice = ordered.subList(from, to);

            Map<Integer, Long> freq = new HashMap<>();
            for (LotteryDraw d : slice) {
                if (d.getNumbers() == null) continue;
                for (int n : d.getNumbers()) freq.merge(n, 1L, (a, b) -> a + b);
            }
            double wEntropy = computeEntropy(freq);
            double wRatio = maxEntropy > 0 ? Math.round(wEntropy / maxEntropy * 10000.0) / 10000.0 : 0;

            windows.add(WindowEntropy.builder()
                    .windowIndex(w)
                    .startDate(slice.get(0).getDrawDate())
                    .endDate(slice.get(slice.size() - 1).getDrawDate())
                    .drawCount(slice.size())
                    .entropy(Math.round(wEntropy * 10000.0) / 10000.0)
                    .entropyRatio(wRatio)
                    .build());
        }

        String interpretation = buildInterpretation(entropyRatio);

        return EntropyAnalysis.builder()
                .lotteryType(type)
                .totalDraws(totalDraws)
                .distinctNumbers(distinctNumbers)
                .observedEntropy(Math.round(observedEntropy * 10000.0) / 10000.0)
                .maxPossibleEntropy(Math.round(maxEntropy * 10000.0) / 10000.0)
                .entropyRatio(entropyRatio)
                .interpretation(interpretation)
                .entropyByWindow(windows)
                .build();
    }

    private double computeEntropy(Map<Integer, Long> freq) {
        long total = freq.values().stream().mapToLong(Long::longValue).sum();
        if (total == 0) return 0;
        double entropy = 0;
        for (long count : freq.values()) {
            if (count > 0) {
                double p = (double) count / total;
                entropy -= p * (Math.log(p) / Math.log(2));
            }
        }
        return entropy;
    }

    private String buildInterpretation(double ratio) {
        if (ratio >= 0.98) return "Distribución casi perfectamente uniforme (entropía ≥ 98% del máximo teórico). No hay evidencia estadística de sesgo en los números.";
        if (ratio >= 0.95) return "Distribución muy uniforme (entropía 95-98%). Ligeras desviaciones pero sin sesgo práctico relevante.";
        if (ratio >= 0.90) return "Distribución mayormente uniforme (entropía 90-95%). Algunos números tienen frecuencias levemente distintas al promedio.";
        if (ratio >= 0.80) return "Distribución moderadamente no uniforme (entropía 80-90%). Hay números significativamente más o menos frecuentes.";
        return "Distribución marcadamente no uniforme (entropía < 80%). Existen números con frecuencias muy distintas. Revisa si los datos son suficientes.";
    }
}
