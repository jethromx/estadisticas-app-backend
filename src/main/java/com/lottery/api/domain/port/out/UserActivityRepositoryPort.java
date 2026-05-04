package com.lottery.api.domain.port.out;

import com.lottery.api.domain.model.UserActivityLog;

import java.util.Map;

public interface UserActivityRepositoryPort {
    void save(UserActivityLog log);
    Map<String, Long> getActionCounts();
}
