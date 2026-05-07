package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class CalendarFrequency {
    LotteryType lotteryType;
    int totalDraws;
    List<DayFrequency> byDayOfWeek;
    List<MonthFrequency> byMonth;
}
