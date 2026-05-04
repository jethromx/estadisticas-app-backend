package com.lottery.api.domain.exception;

public class UserAlreadyExistsException extends LotteryException {
    public UserAlreadyExistsException(String field, String value) {
        super("Ya existe un usuario con " + field + ": " + value);
    }
}
