package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.SavePredictionCommand;
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
    public SavedPrediction execute(SavePredictionCommand command) {
        LocalDate drawDate = (command.latestDrawDate() != null && !command.latestDrawDate().isBlank())
                ? LocalDate.parse(command.latestDrawDate())
                : null;

        SavedPrediction prediction = SavedPrediction.builder()
                .id(UUID.randomUUID().toString())
                .label(command.label())
                .savedAt(LocalDateTime.now())
                .latestDrawDate(drawDate)
                .combosJson(command.combosJson())
                .lotteryType(command.lotteryType())
                .generationParamsJson(command.generationParamsJson())
                .userId(command.userId())
                .build();

        return repository.save(prediction);
    }
}
