package com.lottery.api.infrastructure.adapter.web.dto.response;

import java.util.Map;

public record AdminMetricsResponse(
        long totalUsers,
        long totalPredictions,
        Map<String, Long> actionBreakdown
) {}
