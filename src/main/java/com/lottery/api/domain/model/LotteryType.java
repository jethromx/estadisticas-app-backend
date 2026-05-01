package com.lottery.api.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Tipos de juego de pronósticos de Lotería Nacional México.
 *
 * <p>Cada tipo define su nombre, cantidad de números principales, rango válido y si
 * permite repetición de números (p.ej. GanaGato usa F1-F8 en rango 1-5).</p>
 */
@Getter
@RequiredArgsConstructor
public enum LotteryType {

    /** Melate: 6 números principales (1-56) + 1 adicional. */
    MELATE("Melate", 6, 1, 56, 1, false),

    /** Revancha: 6 números independientes (1-56), sin adicional. */
    REVANCHA("Revancha", 6, 1, 56, 0, false),

    /** Revanchita: 6 números (1-56), sin adicional. */
    REVANCHITA("Revanchita", 6, 1, 56, 0, false),

    /** GanaGato: 8 números en rango 1-5, se permiten repetidos. */
    GANA_GATO("GanaGato", 8, 1, 5, 0, true);

    /** Nombre de presentación del juego. */
    private final String displayName;

    /** Cantidad de números principales en cada sorteo. */
    private final int numbersCount;

    /** Número mínimo válido en el rango del juego. */
    private final int minNumber;

    /** Número máximo válido en el rango del juego. */
    private final int maxNumber;

    /** Cantidad de números adicionales (0 para la mayoría, 1 para Melate). */
    private final int additionalCount;

    /** {@code true} si el sorteo puede contener el mismo número más de una vez. */
    private final boolean allowsDuplicates;

    /** Total de números almacenados por sorteo (principales + adicionales). */
    public int getTotalNumbers() {
        return numbersCount + additionalCount;
    }
}
