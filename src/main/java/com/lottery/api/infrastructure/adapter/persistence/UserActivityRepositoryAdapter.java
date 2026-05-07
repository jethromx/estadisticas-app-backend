package com.lottery.api.infrastructure.adapter.persistence;

import com.lottery.api.domain.model.UserActivityLog;
import com.lottery.api.domain.port.out.UserActivityRepositoryPort;
import com.lottery.api.infrastructure.adapter.persistence.entity.UserActivityLogEntity;
import com.lottery.api.infrastructure.adapter.persistence.projection.ActionCountProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActivityRepositoryAdapter implements UserActivityRepositoryPort {

    private final UserActivityLogJpaRepository jpaRepository;

    @Override
    public Map<String, Long> getActionCounts() {
        return jpaRepository.countByAction().stream()
                .collect(Collectors.toMap(
                        ActionCountProjection::getAction,
                        ActionCountProjection::getCount
                ));
    }

    @Override
    @Async
    public void save(UserActivityLog entry) {
        try {
            jpaRepository.save(UserActivityLogEntity.builder()
                    .userId(entry.getUserId())
                    .endpoint(entry.getEndpoint())
                    .httpMethod(entry.getHttpMethod())
                    .action(entry.getAction())
                    .lotteryType(entry.getLotteryType())
                    .timestamp(entry.getTimestamp())
                    .metadataJson(entry.getMetadataJson())
                    .build());
        } catch (Exception e) {
            log.warn("No se pudo registrar actividad para userId={}: {}", entry.getUserId(), e.getMessage());
        }
    }
}
