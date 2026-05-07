package com.lottery.api.domain.port.in;

import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GetDrawResultsUseCase {
    List<LotteryDraw> execute(LotteryType type, int limit);
    Page<LotteryDraw> executePaged(LotteryType type, Pageable pageable);
}
