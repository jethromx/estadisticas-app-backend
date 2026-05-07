package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@Builder
public class MonthFrequency {
    String monthName;
    int month;
    int drawCount;
    Map<Integer, Long> numberFrequencies;
    List<Integer> hotNumbers;
}
