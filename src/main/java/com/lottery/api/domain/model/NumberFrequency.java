package com.lottery.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Value object que representa la frecuencia de aparición de un número en el histórico.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NumberFrequency {

    private Integer number;

    /** Veces que apareció el número en el histórico consultado. */
    private long frequency;

    /** Porcentaje relativo respecto al total de apariciones del conjunto. */
    private double percentage;

    /** Fecha del último sorteo en que salió este número. */
    private LocalDate lastDrawnDate;

    /** Número de concurso en que apareció por última vez. */
    private Integer lastDrawNumber;

    /**
     * Indica si este número es "caliente" respecto a la frecuencia promedio del conjunto.
     *
     * @param averageFrequency promedio de apariciones del conjunto de números
     * @return {@code true} si la frecuencia supera el promedio
     */
    public boolean isHot(double averageFrequency) {
        return frequency > averageFrequency;
    }
}
