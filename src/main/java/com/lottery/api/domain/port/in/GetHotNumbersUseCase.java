package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.NumberFrequency;

import java.util.List;

/**
 * Puerto de entrada: consulta de números calientes (más frecuentes) y fríos (menos frecuentes).
 */
public interface GetHotNumbersUseCase {

    /**
     * Devuelve los números con mayor frecuencia histórica.
     *
     * @param lotteryType tipo de juego
     * @param limit       cantidad máxima de resultados
     * @return lista ordenada de mayor a menor frecuencia
     */
    List<NumberFrequency> getHotNumbers(LotteryType lotteryType, int limit);

    /**
     * Devuelve los números con menor frecuencia histórica (números "fríos").
     *
     * @param lotteryType tipo de juego
     * @param limit       cantidad máxima de resultados
     * @return lista ordenada de menor a mayor frecuencia
     */
    List<NumberFrequency> getColdNumbers(LotteryType lotteryType, int limit);

    /**
     * Devuelve los números más frecuentes en los últimos N sorteos.
     *
     * @param lotteryType  tipo de juego
     * @param recentDraws  cantidad de sorteos recientes a considerar
     * @param limit        cantidad máxima de resultados
     * @return lista ordenada de mayor a menor frecuencia reciente
     */
    List<NumberFrequency> getRecentHotNumbers(LotteryType lotteryType, int recentDraws, int limit);
}
