package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.ChiSquareResult;
import com.lottery.api.domain.model.LotteryType;

public interface GetChiSquareUseCase {
    ChiSquareResult getChiSquare(LotteryType type);
}
