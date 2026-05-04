package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.SavedPrediction;
import com.lottery.api.domain.port.in.SavePredictionUseCase;
import com.lottery.api.domain.port.out.SavedPredictionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SavePredictionService implements SavePredictionUseCase {

    private final SavedPredictionRepositoryPort repository;

    @Override
    @Transactional
    public SavedPrediction execute(String label, String latestDrawDate, String combosJson) {
        LocalDate drawDate = (latestDrawDate != null && !latestDrawDate.isBlank())
                ? LocalDate.parse(latestDrawDate)
                : null;

        SavedPrediction prediction = SavedPrediction.builder()
                .id(UUID.randomUUID().toString())
                .label(label)
                .savedAt(LocalDateTime.now())
                .latestDrawDate(drawDate)
                .combosJson(combosJson)
                .build();

        return repository.save(prediction);
    }
}
