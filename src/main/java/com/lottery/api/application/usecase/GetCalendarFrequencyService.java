package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.CalendarFrequency;
import com.lottery.api.domain.model.DayFrequency;
import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.MonthFrequency;
import com.lottery.api.domain.port.in.GetCalendarFrequencyUseCase;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class GetCalendarFrequencyService implements GetCalendarFrequencyUseCase {

    private final LotteryDrawRepositoryPort repositoryPort;

    @Override
    @Cacheable(value = "analysis-calendar", key = "#type.name()")
    @Transactional(readOnly = true)
    public CalendarFrequency getCalendarFrequency(LotteryType type) {
        List<LotteryDraw> draws = repositoryPort.findByType(type);

        // day-of-week buckets: 1=Monday..7=Sunday
        Map<Integer, Map<Integer, Long>> byDay   = new TreeMap<>();
        Map<Integer, Integer> dayDrawCount        = new TreeMap<>();
        // month buckets: 1..12
        Map<Integer, Map<Integer, Long>> byMonth  = new TreeMap<>();
        Map<Integer, Integer> monthDrawCount      = new TreeMap<>();

        for (int i = 1; i <= 7; i++)  { byDay.put(i, new HashMap<>()); dayDrawCount.put(i, 0); }
        for (int i = 1; i <= 12; i++) { byMonth.put(i, new HashMap<>()); monthDrawCount.put(i, 0); }

        for (LotteryDraw d : draws) {
            if (d.getDrawDate() == null || d.getNumbers() == null) continue;
            int dow   = d.getDrawDate().getDayOfWeek().getValue();
            int month = d.getDrawDate().getMonthValue();
            dayDrawCount.merge(dow, 1, Integer::sum);
            monthDrawCount.merge(month, 1, Integer::sum);
            for (int n : d.getNumbers()) {
                byDay.get(dow).merge(n, 1L, (a, b) -> a + b);
                byMonth.get(month).merge(n, 1L, (a, b) -> a + b);
            }
        }

        List<DayFrequency> dayList = new ArrayList<>();
        for (int dow = 1; dow <= 7; dow++) {
            int count = dayDrawCount.get(dow);
            if (count == 0) continue;
            Map<Integer, Long> freq = byDay.get(dow);
            List<Integer> hot = freq.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                    .limit(type.getNumbersCount()).map(Map.Entry::getKey).toList();
            dayList.add(DayFrequency.builder()
                    .dayOfWeek(dow)
                    .dayName(DayOfWeek.of(dow).getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es")))
                    .drawCount(count)
                    .numberFrequencies(freq)
                    .hotNumbers(hot)
                    .build());
        }

        List<MonthFrequency> monthList = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            int count = monthDrawCount.get(m);
            if (count == 0) continue;
            Map<Integer, Long> freq = byMonth.get(m);
            List<Integer> hot = freq.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                    .limit(type.getNumbersCount()).map(Map.Entry::getKey).toList();
            monthList.add(MonthFrequency.builder()
                    .month(m)
                    .monthName(Month.of(m).getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es")))
                    .drawCount(count)
                    .numberFrequencies(freq)
                    .hotNumbers(hot)
                    .build());
        }

        return CalendarFrequency.builder()
                .lotteryType(type)
                .totalDraws(draws.size())
                .byDayOfWeek(dayList)
                .byMonth(monthList)
                .build();
    }
}
