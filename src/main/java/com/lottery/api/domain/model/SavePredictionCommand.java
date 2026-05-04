package com.lottery.api.domain.model;

/**
 * Objeto de valor que agrupa todos los parámetros necesarios para guardar una predicción.
 * Evita cambios de firma en el caso de uso cada vez que se agregan campos.
 */
public record SavePredictionCommand(
        String label,
        String latestDrawDate,
        String combosJson,
        LotteryType lotteryType,
        String generationParamsJson,
        String userId
) {}
