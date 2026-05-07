package com.lottery.api.infrastructure.adapter.web.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CalendarFrequencyResponse {
    private String lotteryType;
    private int totalDraws;
    private List<DayFrequencyResponse> byDayOfWeek;
    private List<MonthFrequencyResponse> byMonth;

    @Data
    public static class DayFrequencyResponse {
        private String dayName;
        private int dayOfWeek;
        private int drawCount;
        private Map<Integer, Long> numberFrequencies;
        private List<Integer> hotNumbers;
    }

    @Data
    public static class MonthFrequencyResponse {
        private String monthName;
        private int month;
        private int drawCount;
        private Map<Integer, Long> numberFrequencies;
        private List<Integer> hotNumbers;
    }
}
