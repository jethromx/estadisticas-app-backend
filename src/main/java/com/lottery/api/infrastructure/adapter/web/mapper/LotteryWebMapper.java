package com.lottery.api.infrastructure.adapter.web.mapper;

import com.lottery.api.domain.model.LotteryStatistics;
import com.lottery.api.domain.model.NumberFrequency;
import com.lottery.api.domain.model.PatternSuggestion;
import com.lottery.api.domain.model.SyncResult;
import com.lottery.api.infrastructure.adapter.web.dto.response.NumberFrequencyResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.PatternSuggestionResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.StatisticsResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.SyncResultResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper MapStruct entre modelos de dominio y DTOs de respuesta HTTP.
 */
@Mapper(componentModel = "spring")
public interface LotteryWebMapper {

    @Mapping(target = "lotteryType", expression = "java(result.getLotteryType().name())")
    SyncResultResponse toResponse(SyncResult result);

    @Mapping(target = "lotteryType", expression = "java(stats.getLotteryType().name())")
    StatisticsResponse toResponse(LotteryStatistics stats);

    NumberFrequencyResponse toResponse(NumberFrequency nf);

    @Mapping(target = "lotteryType", expression = "java(suggestion.getLotteryType().name())")
    PatternSuggestionResponse toResponse(PatternSuggestion suggestion);

    List<SyncResultResponse>      toSyncResponseList(List<SyncResult> results);
    List<NumberFrequencyResponse> toFrequencyResponseList(List<NumberFrequency> list);
    List<PatternSuggestionResponse> toPatternResponseList(List<PatternSuggestion> list);
}
