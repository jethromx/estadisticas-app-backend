package com.lottery.api.domain.port.out;

import com.lottery.api.domain.model.SavedPrediction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SavedPredictionRepositoryPort {
    SavedPrediction save(SavedPrediction prediction);
    List<SavedPrediction> findAll();
    List<SavedPrediction> findByUserId(String userId);
    Page<SavedPrediction> findByUserIdPaged(String userId, Pageable pageable);
    Optional<SavedPrediction> findById(String id);
    void deleteById(String id);
    SavedPrediction toggleFavorite(String id, String userId);
}
