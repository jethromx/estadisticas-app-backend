package com.lottery.api.infrastructure.adapter.web.dto.response;

public record DueNumberResponse(
        int    number,
        long   frequency,
        int    lastDrawNumber,
        int    drawsSinceLast,
        double avgInterval,
        double dueScore
) {}
