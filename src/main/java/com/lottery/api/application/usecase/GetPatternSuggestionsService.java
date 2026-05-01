package com.lottery.api.application.usecase;

import com.lottery.api.domain.exception.LotteryException;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.NumberFrequency;
import com.lottery.api.domain.model.PatternSuggestion;
import com.lottery.api.domain.port.in.GetPatternSuggestionsUseCase;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Servicio que genera sugerencias de apuesta mediante cuatro metodologías estadísticas.
 *
 * <p>El {@link Random} es inyectable para garantizar determinismo en los tests.</p>
 */
@Slf4j
@Service
public class GetPatternSuggestionsService implements GetPatternSuggestionsUseCase {

    static final String METHODOLOGY_HOT      = "HOT_NUMBERS";
    static final String METHODOLOGY_COLD     = "COLD_NUMBERS";
    static final String METHODOLOGY_BALANCED = "BALANCED";
    static final String METHODOLOGY_RANDOM   = "STATISTICAL_RANDOM";

    private static final List<String> VALID_METHODOLOGIES =
            List.of(METHODOLOGY_HOT, METHODOLOGY_COLD, METHODOLOGY_BALANCED, METHODOLOGY_RANDOM);

    private static final double CONFIDENCE_HOT      = 0.65;
    private static final double CONFIDENCE_COLD     = 0.45;
    private static final double CONFIDENCE_BALANCED = 0.55;
    private static final double CONFIDENCE_RANDOM   = 0.50;

    private final LotteryDrawRepositoryPort repositoryPort;
    private final Random random;

    @Autowired
    public GetPatternSuggestionsService(LotteryDrawRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
        this.random = new Random();
    }

    /** Constructor para inyección en tests con semilla determinista. */
    GetPatternSuggestionsService(LotteryDrawRepositoryPort repositoryPort, Random random) {
        this.repositoryPort = repositoryPort;
        this.random = random;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatternSuggestion> getPatternSuggestions(LotteryType lotteryType) {
        List<NumberFrequency> freqs = repositoryPort.getNumberFrequencies(lotteryType);
        return List.of(
                buildHotNumbersSuggestion(lotteryType, freqs),
                buildColdNumbersSuggestion(lotteryType, freqs),
                buildBalancedSuggestion(lotteryType, freqs),
                buildStatisticalRandomSuggestion(lotteryType, freqs)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PatternSuggestion getSuggestionByMethodology(LotteryType lotteryType, String methodology) {
        List<NumberFrequency> freqs = repositoryPort.getNumberFrequencies(lotteryType);
        return switch (methodology.toUpperCase()) {
            case METHODOLOGY_HOT      -> buildHotNumbersSuggestion(lotteryType, freqs);
            case METHODOLOGY_COLD     -> buildColdNumbersSuggestion(lotteryType, freqs);
            case METHODOLOGY_BALANCED -> buildBalancedSuggestion(lotteryType, freqs);
            case METHODOLOGY_RANDOM   -> buildStatisticalRandomSuggestion(lotteryType, freqs);
            default -> throw new LotteryException(
                    "Metodología desconocida: '" + methodology +
                    "'. Opciones válidas: " + VALID_METHODOLOGIES);
        };
    }

    private PatternSuggestion buildHotNumbersSuggestion(LotteryType type, List<NumberFrequency> freqs) {
        List<Integer> numbers = topNumbers(freqs, type.getNumbersCount(),
                Comparator.comparingLong(NumberFrequency::getFrequency).reversed());
        numbers = fillWithRepeatsIfNeeded(numbers, type, freqs,
                Comparator.comparingLong(NumberFrequency::getFrequency).reversed());

        return PatternSuggestion.builder()
                .lotteryType(type)
                .suggestedNumbers(numbers)
                .suggestedAdditional(pickAdditional(type, freqs, numbers))
                .methodology(METHODOLOGY_HOT)
                .description("Los " + type.getNumbersCount() + " números con mayor frecuencia histórica")
                .confidenceScore(CONFIDENCE_HOT)
                .build();
    }

    private PatternSuggestion buildColdNumbersSuggestion(LotteryType type, List<NumberFrequency> freqs) {
        List<Integer> numbers = topNumbers(freqs, type.getNumbersCount(),
                Comparator.comparingLong(NumberFrequency::getFrequency));
        numbers = fillWithRepeatsIfNeeded(numbers, type, freqs,
                Comparator.comparingLong(NumberFrequency::getFrequency));

        return PatternSuggestion.builder()
                .lotteryType(type)
                .suggestedNumbers(numbers)
                .suggestedAdditional(pickAdditional(type, freqs, numbers))
                .methodology(METHODOLOGY_COLD)
                .description("Números con menor frecuencia histórica (teoría del retraso)")
                .confidenceScore(CONFIDENCE_COLD)
                .build();
    }

    private PatternSuggestion buildBalancedSuggestion(LotteryType type, List<NumberFrequency> freqs) {
        int hotCount  = type.getNumbersCount() / 2;
        int coldCount = type.getNumbersCount() - hotCount;

        List<Integer> combined = new ArrayList<>();
        combined.addAll(topNumbers(freqs, hotCount,
                Comparator.comparingLong(NumberFrequency::getFrequency).reversed()));
        combined.addAll(topNumbers(freqs, coldCount,
                Comparator.comparingLong(NumberFrequency::getFrequency)));
        combined = fillWithRepeatsIfNeeded(combined, type, freqs,
                Comparator.comparingLong(NumberFrequency::getFrequency).reversed());
        Collections.sort(combined);

        return PatternSuggestion.builder()
                .lotteryType(type)
                .suggestedNumbers(combined)
                .suggestedAdditional(pickAdditional(type, freqs, combined))
                .methodology(METHODOLOGY_BALANCED)
                .description("Combinación equilibrada: " + hotCount + " calientes + " + coldCount + " fríos")
                .confidenceScore(CONFIDENCE_BALANCED)
                .build();
    }

    private PatternSuggestion buildStatisticalRandomSuggestion(LotteryType type, List<NumberFrequency> freqs) {
        long totalFreq = Math.max(1L, freqs.stream().mapToLong(NumberFrequency::getFrequency).sum());

        List<Integer> pool = IntStream.rangeClosed(type.getMinNumber(), type.getMaxNumber())
                .boxed()
                .collect(Collectors.toCollection(ArrayList::new));

        List<Integer> selected = new ArrayList<>();
        while (selected.size() < type.getNumbersCount() && !pool.isEmpty()) {
            int idx = pickWeightedIndex(pool, freqs, totalFreq);
            selected.add(pool.get(idx));
            if (!type.isAllowsDuplicates()) pool.remove(idx);
        }

        Collections.sort(selected);
        return PatternSuggestion.builder()
                .lotteryType(type)
                .suggestedNumbers(selected)
                .suggestedAdditional(pickAdditional(type, freqs, selected))
                .methodology(METHODOLOGY_RANDOM)
                .description("Selección aleatoria ponderada por frecuencia histórica")
                .confidenceScore(CONFIDENCE_RANDOM)
                .build();
    }

    /** Selecciona un índice del pool usando muestreo aleatorio ponderado por frecuencia. */
    private int pickWeightedIndex(List<Integer> pool, List<NumberFrequency> freqs, long totalFreq) {
        double r = random.nextDouble();
        double cumulative = 0.0;
        for (int i = 0; i < pool.size(); i++) {
            cumulative += (double) freqOf(freqs, pool.get(i)) / totalFreq;
            if (r <= cumulative) return i;
        }
        return pool.size() - 1;
    }

    private long freqOf(List<NumberFrequency> freqs, int number) {
        return freqs.stream()
                .filter(nf -> nf.getNumber().equals(number))
                .mapToLong(NumberFrequency::getFrequency)
                .findFirst()
                .orElse(1L);
    }

    private List<Integer> topNumbers(List<NumberFrequency> freqs, int limit,
                                      Comparator<NumberFrequency> order) {
        return freqs.stream()
                .sorted(order)
                .limit(limit)
                .map(NumberFrequency::getNumber)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Para tipos que permiten duplicados (GANA_GATO), rellena la lista repitiendo
     * los números ordenados por el comparador dado hasta alcanzar numbersCount.
     */
    private List<Integer> fillWithRepeatsIfNeeded(List<Integer> numbers, LotteryType type,
                                                   List<NumberFrequency> freqs,
                                                   Comparator<NumberFrequency> order) {
        if (!type.isAllowsDuplicates() || numbers.size() >= type.getNumbersCount()) {
            return numbers;
        }
        List<Integer> result = new ArrayList<>(numbers);
        List<Integer> pool = freqs.stream()
                .sorted(order)
                .map(NumberFrequency::getNumber)
                .collect(Collectors.toList());
        int poolIdx = 0;
        while (result.size() < type.getNumbersCount()) {
            result.add(pool.get(poolIdx % pool.size()));
            poolIdx++;
        }
        Collections.sort(result);
        return result;
    }

    /** Para Melate genera un número adicional distinto de los principales; para otros tipos retorna null. */
    private Integer pickAdditional(LotteryType type, List<NumberFrequency> freqs, List<Integer> mainNumbers) {
        if (type.getAdditionalCount() == 0) return null;
        return freqs.stream()
                .sorted(Comparator.comparingLong(NumberFrequency::getFrequency).reversed())
                .map(NumberFrequency::getNumber)
                .filter(n -> !mainNumbers.contains(n))
                .findFirst()
                .orElse(null);
    }
}
