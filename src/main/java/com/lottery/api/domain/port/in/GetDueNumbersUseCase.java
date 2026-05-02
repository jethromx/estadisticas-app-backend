package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.DueNumber;
import com.lottery.api.domain.model.LotteryType;

import java.util.List;

public interface GetDueNumbersUseCase {

    /**
     * Devuelve los {@code limit} números con mayor probabilidad de salir en el
     * próximo sorteo, ordenados por {@code dueScore} descendente.
     *
     * @param lotteryType tipo de juego
     * @param limit       cantidad de resultados (máx 56)
     */
    List<DueNumber> getDueNumbers(LotteryType lotteryType, int limit);
}
