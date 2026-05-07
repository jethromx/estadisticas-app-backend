package com.lottery.api.application.usecase;

import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.port.in.GetDrawResultsUseCase;
import com.lottery.api.domain.port.out.LotteryDrawRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetDrawResultsService implements GetDrawResultsUseCase {

    private final LotteryDrawRepositoryPort repository;

    @Override
    @Cacheable(value = "draws", key = "#type.name() + '-' + #limit")
    public List<LotteryDraw> execute(LotteryType type, int limit) {
        return repository.findRecentByType(type, limit);
    }

    @Override
    public Page<LotteryDraw> executePaged(LotteryType type, Pageable pageable) {
        return repository.findByTypePageable(type, pageable);
    }
}
