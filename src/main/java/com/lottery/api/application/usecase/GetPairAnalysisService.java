package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.NumberPair;
import com.lottery.api.domain.port.in.GetPairAnalysisUseCase;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPairAnalysisService implements GetPairAnalysisUseCase {

    private final LotteryDrawRepositoryPort repositoryPort;

    @Override
    @Cacheable(value = "analysis-pairs", key = "#type.name() + '-' + #limit")
    @Transactional(readOnly = true)
    public List<NumberPair> getPairAnalysis(LotteryType type, int limit) {
        return repositoryPort.getPairFrequencies(type, limit);
    }
}
