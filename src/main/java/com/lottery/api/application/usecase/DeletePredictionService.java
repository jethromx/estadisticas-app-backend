package com.lottery.api.application.usecase;

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
    public void execute(String id) {
        repository.deleteById(id);
    }
}
