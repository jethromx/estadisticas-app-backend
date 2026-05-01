package com.lottery.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Value object con estadísticas agregadas de un tipo de lotería.
 *
 * <p>Contiene distribución de frecuencias, números calientes/fríos y
 * números que nunca han sido sorteados.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotteryStatistics {

    private LotteryType lotteryType;
    private long totalDraws;
    private LocalDate firstDrawDate;
    private LocalDate lastDrawDate;

    /** Top 10 números más frecuentes en el histórico. */
    private List<NumberFrequency> mostFrequent;

    /** Top 10 números menos frecuentes en el histórico. */
    private List<NumberFrequency> leastFrequent;

    /** Mapa completo: número → total de apariciones. */
    private Map<Integer, Long> frequencyDistribution;

    private double averageFrequency;

    /** Números en el rango válido del juego que nunca han sido sorteados. */
    private List<Integer> numbersNeverDrawn;
}
