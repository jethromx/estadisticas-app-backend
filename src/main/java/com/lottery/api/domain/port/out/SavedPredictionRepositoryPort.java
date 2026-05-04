package com.lottery.api.domain.port.out;

import com.lottery.api.domain.model.SavedPrediction;

import java.util.List;
import java.util.Optional;

public interface SavedPredictionRepositoryPort {
    SavedPrediction save(SavedPrediction prediction);
    List<SavedPrediction> findAll();
    List<SavedPrediction> findByUserId(String userId);
    Optional<SavedPrediction> findById(String id);
    void deleteById(String id);
}
