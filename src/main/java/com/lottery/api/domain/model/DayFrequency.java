package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@Builder
public class DayFrequency {
    String dayName;
    int dayOfWeek;
    int drawCount;
    Map<Integer, Long> numberFrequencies;
    List<Integer> hotNumbers;
}
