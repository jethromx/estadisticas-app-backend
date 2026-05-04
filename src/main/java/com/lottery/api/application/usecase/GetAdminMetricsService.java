package com.lottery.api.application.usecase;

import com.lottery.api.domain.port.in.GetAdminMetricsUseCase;
import com.lottery.api.domain.port.out.SavedPredictionRepositoryPort;
import com.lottery.api.domain.port.out.UserActivityRepositoryPort;
import com.lottery.api.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GetAdminMetricsService implements GetAdminMetricsUseCase {

    private final UserRepositoryPort userRepository;
    private final UserActivityRepositoryPort userActivityRepository;
    private final SavedPredictionRepositoryPort savedPredictionRepository;

    @Override
    public Map<String, Object> execute() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalUsers", (long) userRepository.findAll().size());
        metrics.put("totalPredictions", (long) savedPredictionRepository.findAll().size());
        metrics.put("actionBreakdown", userActivityRepository.getActionCounts());
        return metrics;
    }
}
