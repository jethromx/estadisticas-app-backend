package com.lottery.api.domain.exception;

public class UnauthorizedPredictionAccessException extends LotteryException {
    public UnauthorizedPredictionAccessException() {
        super("No tienes permiso para acceder a esta predicción");
    }
}
