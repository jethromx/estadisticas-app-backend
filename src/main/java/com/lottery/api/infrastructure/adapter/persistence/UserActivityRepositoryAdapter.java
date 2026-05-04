package com.lottery.api.infrastructure.adapter.persistence;

import com.lottery.api.domain.model.UserActivityLog;
import com.lottery.api.domain.port.out.UserActivityRepositoryPort;
import com.lottery.api.infrastructure.adapter.persistence.entity.UserActivityLogEntity;
import com.lottery.api.infrastructure.adapter.persistence.projection.ActionCountProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

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
    public void save(UserActivityLog log) {
        jpaRepository.save(UserActivityLogEntity.builder()
                .userId(log.getUserId())
                .endpoint(log.getEndpoint())
                .httpMethod(log.getHttpMethod())
                .action(log.getAction())
                .lotteryType(log.getLotteryType())
                .timestamp(log.getTimestamp())
                .metadataJson(log.getMetadataJson())
                .build());
    }
}
