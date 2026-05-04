package com.lottery.api.infrastructure.adapter.web.dto.response;

import java.util.List;
import java.util.Map;

public record ComboMatchDetailResponse(
        List<Integer> comboNumbers,
        int bestMatchCount,
        double averageMatchCount,
        Map<String, Integer> matchesPerDraw
) {}
