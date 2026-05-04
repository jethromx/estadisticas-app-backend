package com.lottery.api.domain.exception;

public class PredictionNotFoundException extends LotteryException {
    public PredictionNotFoundException(String id) {
        super("Predicción no encontrada: " + id);
    }
}
