package com.lottery.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Value object que representa una sugerencia de números basada en análisis estadístico.
 *
 * <p>Cada sugerencia incluye la metodología empleada y una puntuación de confianza
 * orientativa (0.0–1.0). <strong>No garantiza resultados.</strong></p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatternSuggestion {

    private LotteryType lotteryType;

    /** Números sugeridos como apuesta principal. */
    private List<Integer> suggestedNumbers;

    /** Número adicional sugerido (solo para Melate). */
    private Integer suggestedAdditional;

    /** Identificador de la metodología: HOT_NUMBERS, COLD_NUMBERS, BALANCED, STATISTICAL_RANDOM. */
    private String methodology;

    /** Descripción en español de la metodología aplicada. */
    private String description;

    /** Puntuación orientativa de confianza estadística entre 0.0 y 1.0. */
    private double confidenceScore;
}
