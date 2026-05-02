package com.lottery.api.infrastructure.adapter.web.mapper;

import com.lottery.api.domain.model.BalanceAnalysis;
import com.lottery.api.domain.model.BacktestResult;
import com.lottery.api.domain.model.BayesianNumber;
import com.lottery.api.domain.model.ChiSquareResult;
import com.lottery.api.domain.model.DueNumber;
import com.lottery.api.domain.model.LotteryStatistics;
import com.lottery.api.domain.model.NumberFrequency;
import com.lottery.api.domain.model.NumberPair;
import com.lottery.api.domain.model.PatternSuggestion;
import com.lottery.api.domain.model.SumDistribution;
import com.lottery.api.domain.model.SyncResult;
import com.lottery.api.domain.model.WindowedFrequency;
import com.lottery.api.infrastructure.adapter.web.dto.response.BacktestResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.BalanceAnalysisResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.BayesianNumberResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.ChiSquareResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.DueNumberResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.NumberFrequencyResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.NumberPairResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.PatternSuggestionResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.StatisticsResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.SumDistributionResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.SyncResultResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.WindowedFrequencyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LotteryWebMapper {

    @Mapping(target = "lotteryType", expression = "java(result.getLotteryType().name())")
    SyncResultResponse toResponse(SyncResult result);

    @Mapping(target = "lotteryType", expression = "java(stats.getLotteryType().name())")
    StatisticsResponse toResponse(LotteryStatistics stats);

    NumberFrequencyResponse toResponse(NumberFrequency nf);

    @Mapping(target = "lotteryType", expression = "java(suggestion.getLotteryType().name())")
    PatternSuggestionResponse toResponse(PatternSuggestion suggestion);

    DueNumberResponse toResponse(DueNumber dueNumber);

    WindowedFrequencyResponse toResponse(WindowedFrequency wf);

    @Mapping(target = "lotteryType", expression = "java(analysis.getLotteryType().name())")
    BalanceAnalysisResponse toResponse(BalanceAnalysis analysis);

    @Mapping(target = "lotteryType", expression = "java(dist.getLotteryType().name())")
    SumDistributionResponse toResponse(SumDistribution dist);

    NumberPairResponse toResponse(NumberPair pair);

    @Mapping(target = "lotteryType", expression = "java(result.getLotteryType().name())")
    ChiSquareResponse toResponse(ChiSquareResult result);

    @Mapping(target = "lotteryType", expression = "java(result.getLotteryType().name())")
    BacktestResponse toResponse(BacktestResult result);

    BayesianNumberResponse toResponse(BayesianNumber bn);

    List<SyncResultResponse>         toSyncResponseList(List<SyncResult> results);
    List<NumberFrequencyResponse>    toFrequencyResponseList(List<NumberFrequency> list);
    List<PatternSuggestionResponse>  toPatternResponseList(List<PatternSuggestion> list);
    List<DueNumberResponse>          toDueNumberResponseList(List<DueNumber> list);
    List<WindowedFrequencyResponse>  toWindowedFrequencyResponseList(List<WindowedFrequency> list);
    List<NumberPairResponse>         toPairResponseList(List<NumberPair> list);
    List<BayesianNumberResponse>     toBayesianResponseList(List<BayesianNumber> list);
}
