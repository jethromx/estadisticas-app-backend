package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.PatternSuggestion;

import java.util.List;

/**
 * Puerto de entrada: generación de sugerencias de números basadas en patrones estadísticos.
 *
 * <p><strong>Aviso:</strong> Las sugerencias son puramente estadísticas y no garantizan
 * resultados en sorteos futuros.</p>
 */
public interface GetPatternSuggestionsUseCase {

    /**
     * Genera sugerencias usando todas las metodologías disponibles:
     * HOT_NUMBERS, COLD_NUMBERS, BALANCED y STATISTICAL_RANDOM.
     *
     * @param lotteryType tipo de juego
     * @return lista de sugerencias, una por metodología
     */
    List<PatternSuggestion> getPatternSuggestions(LotteryType lotteryType);

    /**
     * Genera una única sugerencia con la metodología especificada.
     *
     * @param lotteryType tipo de juego
     * @param methodology nombre de la metodología (insensible a mayúsculas)
     * @return sugerencia generada
     * @throws com.lottery.api.domain.exception.LotteryException si la metodología es desconocida
     */
    PatternSuggestion getSuggestionByMethodology(LotteryType lotteryType, String methodology);
}
