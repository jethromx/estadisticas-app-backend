package com.lottery.api.infrastructure.adapter.persistence;

import com.lottery.api.domain.model.SavedPrediction;
import com.lottery.api.domain.port.out.SavedPredictionRepositoryPort;
import com.lottery.api.infrastructure.adapter.persistence.entity.SavedPredictionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SavedPredictionRepositoryAdapter implements SavedPredictionRepositoryPort {

    private final SavedPredictionJpaRepository jpaRepository;

    @Override
    public SavedPrediction save(SavedPrediction prediction) {
        return toDomain(jpaRepository.save(toEntity(prediction)));
    }

    @Override
    public List<SavedPrediction> findAll() {
        return jpaRepository.findAllByOrderBySavedAtDesc()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<SavedPrediction> findByUserId(String userId) {
        return jpaRepository.findByUserIdOrderBySavedAtDesc(userId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<SavedPrediction> findById(String id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Page<SavedPrediction> findByUserIdPaged(String userId, Pageable pageable) {
        return jpaRepository.findByUserIdOrderBySavedAtDesc(userId, pageable)
                .map(this::toDomain);
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public SavedPrediction toggleFavorite(String id, String userId) {
        SavedPredictionEntity entity = jpaRepository.findById(id)
                .filter(e -> Objects.equals(userId, e.getUserId()))
                .orElseThrow(() -> new IllegalArgumentException("Prediction not found: " + id));
        entity.setFavorite(!entity.isFavorite());
        return toDomain(jpaRepository.save(entity));
    }

    private SavedPredictionEntity toEntity(SavedPrediction p) {
        return SavedPredictionEntity.builder()
                .id(p.getId())
                .label(p.getLabel())
                .savedAt(p.getSavedAt())
                .latestDrawDate(p.getLatestDrawDate())
                .combosJson(p.getCombosJson())
                .lotteryType(p.getLotteryType())
                .generationParamsJson(p.getGenerationParamsJson())
                .userId(p.getUserId())
                .favorite(p.isFavorite())
                .build();
    }

    private SavedPrediction toDomain(SavedPredictionEntity e) {
        return SavedPrediction.builder()
                .id(e.getId())
                .label(e.getLabel())
                .savedAt(e.getSavedAt())
                .latestDrawDate(e.getLatestDrawDate())
                .combosJson(e.getCombosJson())
                .lotteryType(e.getLotteryType())
                .generationParamsJson(e.getGenerationParamsJson())
                .userId(e.getUserId())
                .favorite(e.isFavorite())
                .build();
    }
}
