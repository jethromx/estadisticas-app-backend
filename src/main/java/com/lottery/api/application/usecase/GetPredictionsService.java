package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.SavedPrediction;
import com.lottery.api.domain.port.in.GetPredictionsUseCase;
import com.lottery.api.domain.port.out.SavedPredictionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPredictionsService implements GetPredictionsUseCase {

    private final SavedPredictionRepositoryPort repository;

    @Override
    @Transactional(readOnly = true)
    public List<SavedPrediction> execute(String userId) {
        return repository.findByUserId(userId);
    }
}
