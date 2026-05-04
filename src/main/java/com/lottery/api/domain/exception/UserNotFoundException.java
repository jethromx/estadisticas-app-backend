package com.lottery.api.domain.exception;

public class UserNotFoundException extends LotteryException {
    public UserNotFoundException(String userId) {
        super("Usuario no encontrado con ID: " + userId);
    }
}