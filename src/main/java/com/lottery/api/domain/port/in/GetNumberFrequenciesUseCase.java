package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.NumberFrequency;

import java.util.List;

/**
 * Puerto de entrada: consulta de frecuencia de aparición de cada número.
 */
public interface GetNumberFrequenciesUseCase {

    /**
     * Devuelve la frecuencia de todos los números del rango válido del juego,
     * ordenados por número ascendente.
     *
     * @param lotteryType tipo de juego
     * @return lista completa de frecuencias
     */
    List<NumberFrequency> getNumberFrequencies(LotteryType lotteryType);

    /**
     * Devuelve la frecuencia de un número específico.
     *
     * @param lotteryType tipo de juego
     * @param number      número a consultar (debe estar en el rango válido del juego)
     * @return frecuencia del número; si nunca fue sorteado, frecuencia = 0
     * @throws com.lottery.api.domain.exception.LotteryException si el número está fuera de rango
     */
    NumberFrequency getNumberFrequency(LotteryType lotteryType, int number);
}
