package com.lottery.api.domain.exception;

public class InvalidCredentialsException extends LotteryException {
    public InvalidCredentialsException() {
        super("Credenciales inválidas");
    }
}
