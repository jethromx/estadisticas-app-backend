package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.BalanceAnalysis;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.port.in.GetBalanceAnalysisUseCase;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetBalanceAnalysisService implements GetBalanceAnalysisUseCase {

    private final LotteryDrawRepositoryPort repositoryPort;

    @Override
    @Cacheable(value = "analysis-balance", key = "#lotteryType.name()")
    @Transactional(readOnly = true)
    public BalanceAnalysis getBalanceAnalysis(LotteryType lotteryType) {
        return repositoryPort.getBalanceAnalysis(lotteryType);
    }
}
