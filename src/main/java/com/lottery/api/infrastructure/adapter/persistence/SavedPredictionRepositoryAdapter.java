package com.lottery.api.infrastructure.adapter.persistence;

import com.lottery.api.domain.model.SavedPrediction;
import com.lottery.api.domain.port.out.SavedPredictionRepositoryPort;
import com.lottery.api.infrastructure.adapter.persistence.entity.SavedPredictionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

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
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    private SavedPredictionEntity toEntity(SavedPrediction p) {
        return SavedPredictionEntity.builder()
                .id(p.getId())
                .label(p.getLabel())
                .savedAt(p.getSavedAt())
                .latestDrawDate(p.getLatestDrawDate())
                .combosJson(p.getCombosJson())
                .userId(p.getUserId())
                .build();
    }

    private SavedPrediction toDomain(SavedPredictionEntity e) {
        return SavedPrediction.builder()
                .id(e.getId())
                .label(e.getLabel())
                .savedAt(e.getSavedAt())
                .latestDrawDate(e.getLatestDrawDate())
                .combosJson(e.getCombosJson())
                .userId(e.getUserId())
                .build();
    }
}
