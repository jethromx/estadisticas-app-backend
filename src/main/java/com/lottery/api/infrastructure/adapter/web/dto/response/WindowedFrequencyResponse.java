package com.lottery.api.infrastructure.adapter.web.dto.response;

public record WindowedFrequencyResponse(
        int number,
        int frequency,
        double percentage,
        int windowSize,
        int windowDrawCount,
        double trend
) {}
