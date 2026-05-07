package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.SumDistribution;
import com.lottery.api.domain.port.in.GetSumDistributionUseCase;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetSumDistributionService implements GetSumDistributionUseCase {

    private final LotteryDrawRepositoryPort repositoryPort;

    @Override
    @Cacheable(value = "analysis-sum", key = "#lotteryType.name()")
    @Transactional(readOnly = true)
    public SumDistribution getSumDistribution(LotteryType lotteryType) {
        return repositoryPort.getSumDistribution(lotteryType);
    }
}
