package com.lottery.api.domain.port.out;

import com.lottery.api.domain.model.SavedPrediction;

import java.util.List;

public interface SavedPredictionRepositoryPort {
    SavedPrediction save(SavedPrediction prediction);
    List<SavedPrediction> findAll();
    void deleteById(String id);
}
