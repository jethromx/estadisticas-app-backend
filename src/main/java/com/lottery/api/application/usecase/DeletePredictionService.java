package com.lottery.api.application.usecase;

import com.lottery.api.domain.exception.PredictionNotFoundException;
import com.lottery.api.domain.exception.UnauthorizedPredictionAccessException;
import com.lottery.api.domain.model.SavedPrediction;
import com.lottery.api.domain.port.in.DeletePredictionUseCase;
import com.lottery.api.domain.port.out.SavedPredictionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeletePredictionService implements DeletePredictionUseCase {

    private final SavedPredictionRepositoryPort repository;

    @Override
    @Transactional
    public void execute(String id, String requestingUserId) {
        SavedPrediction prediction = repository.findById(id)
                .orElseThrow(() -> new PredictionNotFoundException(id));

        if (prediction.getUserId() != null && !prediction.getUserId().equals(requestingUserId)) {
            throw new UnauthorizedPredictionAccessException();
        }

        repository.deleteById(id);
    }
}
