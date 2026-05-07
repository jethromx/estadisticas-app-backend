package com.lottery.api.application.usecase;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lottery.api.domain.exception.PredictionNotFoundException;
import com.lottery.api.domain.exception.UnauthorizedPredictionAccessException;
import com.lottery.api.domain.model.ComboMatchDetail;
import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.PredictionAccuracyResult;
import com.lottery.api.domain.model.SavedPrediction;
import com.lottery.api.domain.port.in.AnalyzePredictionAccuracyUseCase;
import com.lottery.api.domain.port.in.SyncHistoricalDataUseCase;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import com.lottery.api.domain.port.out.SavedPredictionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyzePredictionAccuracyService implements AnalyzePredictionAccuracyUseCase {

    private static final TypeReference<List<List<Integer>>> ARRAY_COMBOS_TYPE = new TypeReference<>() {};
    private static final double HOT_NUMBER_THRESHOLD = 0.2;
    private static final double SKEW_THRESHOLD = 0.4;

    private final SavedPredictionRepositoryPort predictionRepository;
    private final LotteryDrawRepositoryPort drawRepository;
    private final SyncHistoricalDataUseCase syncUseCase;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public PredictionAccuracyResult execute(String predictionId, String requestingUserId, boolean syncFirst) {
        SavedPrediction prediction = predictionRepository.findById(predictionId)
                .orElseThrow(() -> new PredictionNotFoundException(predictionId));

        if (prediction.getUserId() != null && !prediction.getUserId().equals(requestingUserId)) {
            throw new UnauthorizedPredictionAccessException();
        }

        if (prediction.getLotteryType() == null) {
            throw new IllegalStateException("La predicción no tiene tipo de lotería asignado");
        }
        LotteryType lotteryType = prediction.getLotteryType();

        if (syncFirst) {
            try {
                syncUseCase.syncHistoricalData(lotteryType);
            } catch (Exception e) {
                log.warn("Sync antes de análisis falló (continuando con datos existentes): {}", e.getMessage());
            }
        }

        List<LotteryDraw> draws = drawRepository.findDrawsAfterDate(lotteryType, prediction.getLatestDrawDate());

        if (draws.isEmpty()) {
            return PredictionAccuracyResult.builder()
                    .predictionId(predictionId)
                    .predictionLabel(prediction.getLabel())
                    .lotteryType(lotteryType)
                    .drawsAnalyzed(0)
                    .bestMatchCount(0)
                    .worstMatchCount(0)
                    .averageMatchCount(0)
                    .comboDetails(List.of())
                    .improvementSuggestions(List.of("No existen sorteos posteriores a la fecha de guardado para comparar."))
                    .build();
        }

        List<List<Integer>> combos = parseCombos(prediction.getCombosJson());
        List<ComboMatchDetail> comboDetails = combos.stream()
                .map(combo -> buildComboDetail(combo, draws))
                .toList();

        int best = comboDetails.stream().mapToInt(ComboMatchDetail::getBestMatchCount).max().orElse(0);
        int worst = comboDetails.stream().mapToInt(ComboMatchDetail::getBestMatchCount).min().orElse(0);
        double avg = comboDetails.stream().mapToDouble(ComboMatchDetail::getAverageMatchCount).average().orElse(0);

        List<String> suggestions = buildSuggestions(combos, draws, lotteryType, best);

        return PredictionAccuracyResult.builder()
                .predictionId(predictionId)
                .predictionLabel(prediction.getLabel())
                .lotteryType(lotteryType)
                .drawsAnalyzed(draws.size())
                .bestMatchCount(best)
                .worstMatchCount(worst)
                .averageMatchCount(Math.round(avg * 100.0) / 100.0)
                .comboDetails(comboDetails)
                .improvementSuggestions(suggestions)
                .build();
    }

    private ComboMatchDetail buildComboDetail(List<Integer> combo, List<LotteryDraw> draws) {
        Set<Integer> comboSet = Set.copyOf(combo);
        Map<LocalDate, Integer> matchesPerDraw = new LinkedHashMap<>();
        int best = 0;

        for (LotteryDraw draw : draws) {
            int matches = (int) draw.getNumbers().stream().filter(comboSet::contains).count();
            matchesPerDraw.put(draw.getDrawDate(), matches);
            if (matches > best) best = matches;
        }

        double avgMatches = matchesPerDraw.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);

        return ComboMatchDetail.builder()
                .comboNumbers(combo)
                .bestMatchCount(best)
                .averageMatchCount(Math.round(avgMatches * 100.0) / 100.0)
                .matchesPerDraw(matchesPerDraw)
                .build();
    }

    private List<String> buildSuggestions(
            List<List<Integer>> combos,
            List<LotteryDraw> draws,
            LotteryType type,
            int bestOverall) {

        List<String> suggestions = new ArrayList<>();

        if (bestOverall == 0) {
            suggestions.add("Ninguna combinación logró al menos 1 acierto. Considera revisar el algoritmo de generación.");
        } else if (bestOverall < 3) {
            suggestions.add("El mejor resultado fue " + bestOverall + " aciertos. Considera incrementar la ventana de sorteos usada para el análisis estadístico.");
        }

        Map<Integer, Long> drawnFreq = draws.stream()
                .flatMap(d -> d.getNumbers().stream())
                .collect(Collectors.groupingBy(n -> n, Collectors.counting()));

        long totalDrawnNumbers = drawnFreq.values().stream().mapToLong(Long::longValue).sum();
        int topCount = Math.max(1, (int) Math.ceil(type.getMaxNumber() * HOT_NUMBER_THRESHOLD));
        Set<Integer> hotNumbers = drawnFreq.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(topCount)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        long predictionNumbers = combos.stream().flatMap(List::stream).distinct().count();
        long hotInPrediction = combos.stream()
                .flatMap(List::stream)
                .distinct()
                .filter(hotNumbers::contains)
                .count();

        double hotCoverage = predictionNumbers > 0 ? (double) hotInPrediction / predictionNumbers : 0;

        long hotInDraws = draws.stream()
                .flatMap(d -> d.getNumbers().stream())
                .filter(hotNumbers::contains)
                .count();
        double hotDrawRatio = totalDrawnNumbers > 0 ? (double) hotInDraws / totalDrawnNumbers : 0;

        if (hotDrawRatio > 0.5 && hotCoverage < SKEW_THRESHOLD) {
            suggestions.add(String.format(
                    "Los sorteos analizados tuvieron %.0f%% de números 'calientes', pero tus combinaciones solo los cubren en un %.0f%%. Considera incluir más números frecuentes.",
                    hotDrawRatio * 100, hotCoverage * 100));
        } else if (hotDrawRatio < 0.3 && hotCoverage > 0.6) {
            suggestions.add("Los sorteos recientes favorecieron números fríos, pero tus combinaciones se concentran en números calientes. Considera diversificar.");
        }

        boolean hasBalancedOddEven = combos.stream().anyMatch(combo -> {
            long odd = combo.stream().filter(n -> n % 2 != 0).count();
            return odd >= 2 && odd <= combo.size() - 2;
        });
        if (!hasBalancedOddEven) {
            suggestions.add("Tus combinaciones no tienen balance par/impar. Intenta incluir aproximadamente la mitad de números pares e impares.");
        }

        if (suggestions.isEmpty()) {
            suggestions.add("Buen desempeño general. Mantén la estrategia de generación y sigue sincronizando para evaluar nuevos sorteos.");
        }

        return suggestions;
    }

    private List<List<Integer>> parseCombos(String combosJson) {
        try {
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(combosJson);
            if (!root.isArray()) return List.of();

            List<List<Integer>> result = new ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode node : root) {
                if (node.isArray()) {
                    // legacy format: [[1,2,3,...]]
                    List<Integer> nums = new ArrayList<>();
                    node.forEach(n -> nums.add(n.asInt()));
                    result.add(nums);
                } else if (node.isObject() && node.has("numbers")) {
                    // current format: [{"numbers":[1,2,3,...],...}]
                    List<Integer> nums = new ArrayList<>();
                    node.get("numbers").forEach(n -> nums.add(n.asInt()));
                    result.add(nums);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("Error parseando combosJson: {}", e.getMessage());
            return List.of();
        }
    }
}
