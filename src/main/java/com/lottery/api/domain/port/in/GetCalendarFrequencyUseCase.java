package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.CalendarFrequency;
import com.lottery.api.domain.model.LotteryType;

public interface GetCalendarFrequencyUseCase {
    CalendarFrequency getCalendarFrequency(LotteryType type);
}
