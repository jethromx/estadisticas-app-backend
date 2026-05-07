package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.DueNumber;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.port.in.GetDueNumbersUseCase;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetDueNumbersService implements GetDueNumbersUseCase {

    private final LotteryDrawRepositoryPort repositoryPort;

    @Override
    @Cacheable(value = "analysis-due", key = "#lotteryType.name() + '-' + #limit")
    @Transactional(readOnly = true)
    public List<DueNumber> getDueNumbers(LotteryType lotteryType, int limit) {
        return repositoryPort.getDueNumbers(lotteryType, limit);
    }
}
