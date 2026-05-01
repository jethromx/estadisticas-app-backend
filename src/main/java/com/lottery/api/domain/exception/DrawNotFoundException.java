package com.lottery.api.domain.exception;

import com.lottery.api.domain.model.LotteryType;

/**
 * Se lanza cuando no se encuentra un sorteo específico en el repositorio.
 */
public class DrawNotFoundException extends LotteryException {

    public DrawNotFoundException(LotteryType type, Integer drawNumber) {
        super(String.format("Sorteo no encontrado: tipo=%s, concurso=%d", type, drawNumber));
    }

    public DrawNotFoundException(Long id) {
        super(String.format("Sorteo no encontrado con id=%d", id));
    }
}
