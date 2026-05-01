package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.LotteryStatistics;
import com.lottery.api.domain.model.LotteryType;

import java.time.LocalDate;

/**
 * Puerto de entrada: consulta de estadísticas agregadas de un tipo de lotería.
 */
public interface GetStatisticsUseCase {

    /**
     * Obtiene estadísticas completas sobre todo el histórico disponible.
     *
     * @param lotteryType tipo de juego
     * @return estadísticas con frecuencias, promedios y números nunca sorteados
     */
    LotteryStatistics getStatistics(LotteryType lotteryType);

    /**
     * Obtiene estadísticas limitadas a un rango de fechas.
     *
     * @param lotteryType tipo de juego
     * @param from        fecha de inicio (inclusiva)
     * @param to          fecha de fin (inclusiva)
     * @return estadísticas del período indicado
     */
    LotteryStatistics getStatisticsByDateRange(LotteryType lotteryType, LocalDate from, LocalDate to);
}
