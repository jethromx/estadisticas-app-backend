package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.WindowedFrequency;

import java.util.List;

public interface GetWindowedFrequenciesUseCase {
    List<WindowedFrequency> getWindowedFrequencies(LotteryType lotteryType, int windowSize);
}
