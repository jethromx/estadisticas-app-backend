package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.SavedPrediction;
import com.lottery.api.domain.port.in.GetPredictionsUseCase;
import com.lottery.api.domain.port.out.SavedPredictionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Override
    @Transactional(readOnly = true)
    public Page<SavedPrediction> executePaged(String userId, Pageable pageable) {
        return repository.findByUserIdPaged(userId, pageable);
    }
}
