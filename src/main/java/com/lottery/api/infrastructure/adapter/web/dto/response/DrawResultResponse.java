package com.lottery.api.infrastructure.adapter.web.dto.response;

import java.time.LocalDate;
import java.util.List;

public record DrawResultResponse(
        Integer drawNumber,
        LocalDate drawDate,
        List<Integer> numbers
) {}
