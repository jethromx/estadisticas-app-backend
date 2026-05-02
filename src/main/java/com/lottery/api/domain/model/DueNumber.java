package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

/**
 * Número "pendiente" de salir según el algoritmo de intervalo promedio.
 *
 * <p>{@code dueScore = drawsSinceLast / avgInterval}. Un valor > 1.0 indica que
 * el número ya superó su intervalo promedio de aparición.</p>
 */
@Value
@Builder
public class DueNumber {
    int    number;
    long   frequency;
    int    lastDrawNumber;
    int    drawsSinceLast;
    double avgInterval;
    double dueScore;
}
