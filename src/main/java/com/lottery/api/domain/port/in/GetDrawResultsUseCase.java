package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;

import java.util.List;

public interface GetDrawResultsUseCase {
    List<LotteryDraw> execute(LotteryType type, int limit);
}
