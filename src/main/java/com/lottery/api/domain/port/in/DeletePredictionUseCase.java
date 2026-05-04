package com.lottery.api.domain.port.in;

public interface DeletePredictionUseCase {
    void execute(String id, String requestingUserId);
}
