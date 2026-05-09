package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.SavedPrediction;
import com.lottery.api.domain.port.in.ToggleFavoritePredictionUseCase;
import com.lottery.api.domain.port.out.SavedPredictionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ToggleFavoritePredictionService implements ToggleFavoritePredictionUseCase {

    private final SavedPredictionRepositoryPort repository;

    @Override
    @Transactional
    public SavedPrediction execute(String predictionId, String userId) {
        return repository.toggleFavorite(predictionId, userId);
    }
}
