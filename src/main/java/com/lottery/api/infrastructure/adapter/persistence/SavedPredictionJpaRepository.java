package com.lottery.api.infrastructure.adapter.persistence;

import com.lottery.api.infrastructure.adapter.persistence.entity.SavedPredictionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedPredictionJpaRepository extends JpaRepository<SavedPredictionEntity, String> {

    List<SavedPredictionEntity> findAllByOrderBySavedAtDesc();

    List<SavedPredictionEntity> findByUserIdOrderBySavedAtDesc(String userId);

    Page<SavedPredictionEntity> findByUserIdOrderBySavedAtDesc(String userId, Pageable pageable);
}
