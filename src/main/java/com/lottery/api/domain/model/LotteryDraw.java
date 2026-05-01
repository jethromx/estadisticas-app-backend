package com.lottery.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entidad de dominio que representa un sorteo de lotería.
 *
 * <p>Actúa como raíz de agregado. Encapsula los números sorteados y los metadatos
 * del concurso. Los números principales están en {@link #numbers}; el adicional
 * de Melate (R7) en {@link #additionalNumber}.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotteryDraw {

    private Long id;
    private LotteryType lotteryType;

    /** Número de concurso (identificador incremental del sorteo). */
    private Integer drawNumber;

    private LocalDate drawDate;

    /** Números principales del sorteo, en el orden en que aparecen en el CSV. */
    private List<Integer> numbers;

    /** Número adicional exclusivo de Melate (campo R7 del CSV). */
    private Integer additionalNumber;

    private BigDecimal jackpotAmount;
    private Integer firstPrizeWinners;

    /**
     * Verifica si un número específico es parte de los números principales.
     *
     * @param number número a buscar
     * @return {@code true} si está en la lista de números principales
     */
    public boolean containsNumber(int number) {
        return numbers != null && numbers.contains(number);
    }

    /**
     * Retorna todos los números del sorteo: principales + adicional si existe.
     *
     * @return lista inmutable con todos los números
     */
    public List<Integer> getAllNumbers() {
        if (additionalNumber == null || numbers == null) {
            return numbers != null ? Collections.unmodifiableList(numbers) : Collections.emptyList();
        }
        List<Integer> all = new ArrayList<>(numbers);
        all.add(additionalNumber);
        return Collections.unmodifiableList(all);
    }
}
