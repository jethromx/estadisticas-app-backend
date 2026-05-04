package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Value
@Builder
public class ComboMatchDetail {
    List<Integer> comboNumbers;
    int bestMatchCount;
    double averageMatchCount;
    Map<LocalDate, Integer> matchesPerDraw;
}
