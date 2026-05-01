package com.lottery.api.domain.exception;

/**
 * Excepción base para todos los errores del dominio de lotería.
 */
public class LotteryException extends RuntimeException {

    public LotteryException(String message) {
        super(message);
    }

    public LotteryException(String message, Throwable cause) {
        super(message, cause);
    }
}
