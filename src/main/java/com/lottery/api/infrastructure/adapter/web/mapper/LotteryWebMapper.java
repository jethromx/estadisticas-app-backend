package com.lottery.api.infrastructure.adapter.web.mapper;

import com.lottery.api.domain.model.BalanceAnalysis;
import com.lottery.api.domain.model.BacktestResult;
import com.lottery.api.domain.model.BayesianNumber;
import com.lottery.api.domain.model.CalendarFrequency;
import com.lottery.api.domain.model.ChiSquareResult;
import com.lottery.api.domain.model.ClusterAnalysis;
import com.lottery.api.domain.model.ConsecutiveAnalysis;
import com.lottery.api.domain.model.DueNumber;
import com.lottery.api.domain.model.EnsemblePrediction;
import com.lottery.api.domain.model.EntropyAnalysis;
import com.lottery.api.domain.model.LotteryStatistics;
import com.lottery.api.domain.model.NumberFrequency;
import com.lottery.api.domain.model.NumberPair;
import com.lottery.api.domain.model.PatternSuggestion;
import com.lottery.api.domain.model.PositionAnalysis;
import com.lottery.api.domain.model.RichBacktestResult;
import com.lottery.api.domain.model.SumDistribution;
import com.lottery.api.domain.model.SumStreakAnalysis;
import com.lottery.api.domain.model.SyncResult;
import com.lottery.api.domain.model.TemporalWeightResult;
import com.lottery.api.domain.model.WindowedFrequency;
import com.lottery.api.infrastructure.adapter.web.dto.response.BacktestResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.BalanceAnalysisResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.BayesianNumberResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.CalendarFrequencyResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.ChiSquareResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.ClusterAnalysisResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.ConsecutiveAnalysisResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.DueNumberResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.EnsemblePredictionResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.EntropyAnalysisResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.NumberFrequencyResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.NumberPairResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.PatternSuggestionResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.PositionAnalysisResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.RichBacktestResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.StatisticsResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.SumDistributionResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.SumStreakResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.SyncResultResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.TemporalWeightResponse;
import com.lottery.api.infrastructure.adapter.web.dto.response.WindowedFrequencyResponse;
import com.lottery.api.domain.model.NeuralPrediction;
import com.lottery.api.infrastructure.adapter.web.dto.response.NeuralPredictionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface LotteryWebMapper {

    // ---- Existing mappings (MapStruct auto-generated) -----------------------

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

    // ---- New mappings (manual default methods for complex nested types) ------

    default PositionAnalysisResponse toResponse(PositionAnalysis src) {
        if (src == null) return null;
        PositionAnalysisResponse r = new PositionAnalysisResponse();
        r.setLotteryType(src.getLotteryType().name());
        r.setTotalDraws(src.getTotalDraws());
        r.setPositions(src.getPositions().stream().map(p -> {
            PositionAnalysisResponse.PositionStatsResponse pr = new PositionAnalysisResponse.PositionStatsResponse();
            pr.setPosition(p.getPosition());
            pr.setMean(p.getMean());
            pr.setStdDev(p.getStdDev());
            pr.setMin(p.getMin());
            pr.setMax(p.getMax());
            pr.setP10(p.getP10()); pr.setP25(p.getP25()); pr.setP50(p.getP50());
            pr.setP75(p.getP75()); pr.setP90(p.getP90());
            pr.setRecommendedMin(p.getRecommendedMin());
            pr.setRecommendedMax(p.getRecommendedMax());
            return pr;
        }).collect(Collectors.toList()));
        return r;
    }

    default ConsecutiveAnalysisResponse toResponse(ConsecutiveAnalysis src) {
        if (src == null) return null;
        ConsecutiveAnalysisResponse r = new ConsecutiveAnalysisResponse();
        r.setLotteryType(src.getLotteryType().name());
        r.setTotalDraws(src.getTotalDraws());
        r.setDrawsWithAtLeastOne(src.getDrawsWithAtLeastOne());
        r.setConsecutiveRate(src.getConsecutiveRate());
        r.setDistributionByCount(src.getDistributionByCount());
        r.setAvgPairsPerDraw(src.getAvgPairsPerDraw());
        r.setTopPairs(src.getTopPairs().stream().map(p -> {
            ConsecutiveAnalysisResponse.ConsecutivePairResponse pr = new ConsecutiveAnalysisResponse.ConsecutivePairResponse();
            pr.setLower(p.getLower()); pr.setHigher(p.getHigher());
            pr.setFrequency(p.getFrequency()); pr.setPercentage(p.getPercentage());
            return pr;
        }).collect(Collectors.toList()));
        return r;
    }

    default RichBacktestResponse toResponse(RichBacktestResult src) {
        if (src == null) return null;
        RichBacktestResponse r = new RichBacktestResponse();
        r.setLotteryType(src.getLotteryType().name());
        r.setTestDraws(src.getTestDraws());
        r.setTopK(src.getTopK());
        r.setBestStrategy(src.getBestStrategy());
        r.setBestHitRate(src.getBestHitRate());
        r.setStrategies(src.getStrategies().stream().map(s -> {
            RichBacktestResponse.StrategyResultResponse sr = new RichBacktestResponse.StrategyResultResponse();
            sr.setStrategyName(s.getStrategyName());
            sr.setNumbers(s.getNumbers());
            sr.setMatchDistribution(s.getMatchDistribution());
            sr.setHitRate(s.getHitRate());
            sr.setAvgMatches(s.getAvgMatches());
            sr.setExpectedRandomRate(s.getExpectedRandomRate());
            sr.setVsRandom(s.getVsRandom());
            sr.setRollingHitRates(s.getRollingHitRates());
            return sr;
        }).collect(Collectors.toList()));
        return r;
    }

    default TemporalWeightResponse toResponse(TemporalWeightResult src) {
        if (src == null) return null;
        TemporalWeightResponse r = new TemporalWeightResponse();
        r.setLotteryType(src.getLotteryType().name());
        r.setTotalDraws(src.getTotalDraws());
        r.setDecayFactors(src.getDecayFactors());
        r.setRecommendation(src.getRecommendation());
        r.setNumbers(src.getNumbers().stream().map(n -> {
            TemporalWeightResponse.WeightedNumberResponse nr = new TemporalWeightResponse.WeightedNumberResponse();
            nr.setNumber(n.getNumber());
            nr.setRawFrequency(n.getRawFrequency());
            nr.setRawFrequencyPct(n.getRawFrequencyPct());
            nr.setWeightedScores(n.getWeightedScores());
            nr.setRankByFrequency(n.getRankByFrequency());
            nr.setRankByWeight099(n.getRankByWeight099());
            nr.setRankByWeight090(n.getRankByWeight090());
            return nr;
        }).collect(Collectors.toList()));
        return r;
    }

    default EntropyAnalysisResponse toResponse(EntropyAnalysis src) {
        if (src == null) return null;
        EntropyAnalysisResponse r = new EntropyAnalysisResponse();
        r.setLotteryType(src.getLotteryType().name());
        r.setTotalDraws(src.getTotalDraws());
        r.setDistinctNumbers(src.getDistinctNumbers());
        r.setObservedEntropy(src.getObservedEntropy());
        r.setMaxPossibleEntropy(src.getMaxPossibleEntropy());
        r.setEntropyRatio(src.getEntropyRatio());
        r.setInterpretation(src.getInterpretation());
        r.setEntropyByWindow(src.getEntropyByWindow().stream().map(w -> {
            EntropyAnalysisResponse.WindowEntropyResponse wr = new EntropyAnalysisResponse.WindowEntropyResponse();
            wr.setWindowIndex(w.getWindowIndex());
            wr.setStartDate(w.getStartDate()); wr.setEndDate(w.getEndDate());
            wr.setDrawCount(w.getDrawCount());
            wr.setEntropy(w.getEntropy()); wr.setEntropyRatio(w.getEntropyRatio());
            return wr;
        }).collect(Collectors.toList()));
        return r;
    }

    default ClusterAnalysisResponse toResponse(ClusterAnalysis src) {
        if (src == null) return null;
        ClusterAnalysisResponse r = new ClusterAnalysisResponse();
        r.setLotteryType(src.getLotteryType().name());
        r.setTotalDraws(src.getTotalDraws());
        r.setK(src.getK());
        r.setInterpretation(src.getInterpretation());
        r.setClusters(src.getClusters().stream().map(c -> {
            ClusterAnalysisResponse.DrawClusterResponse cr = new ClusterAnalysisResponse.DrawClusterResponse();
            cr.setClusterId(c.getClusterId());
            cr.setDrawCount(c.getDrawCount());
            cr.setCentroidSum(c.getCentroidSum());
            cr.setCentroidOddCount(c.getCentroidOddCount());
            cr.setCentroidSpread(c.getCentroidSpread());
            cr.setMostCommonNumbers(c.getMostCommonNumbers());
            cr.setPctOfTotal(c.getPctOfTotal());
            return cr;
        }).collect(Collectors.toList()));
        return r;
    }

    default SumStreakResponse toResponse(SumStreakAnalysis src) {
        if (src == null) return null;
        SumStreakResponse r = new SumStreakResponse();
        r.setLotteryType(src.getLotteryType().name());
        r.setTotalDraws(src.getTotalDraws());
        r.setMeanSum(src.getMeanSum());
        r.setStdDevSum(src.getStdDevSum());
        r.setLongestHighStreak(src.getLongestHighStreak());
        r.setLongestLowStreak(src.getLongestLowStreak());
        if (src.getCurrentStreak() != null) r.setCurrentStreak(mapStreak(src.getCurrentStreak()));
        r.setStreakHistory(src.getStreakHistory().stream().map(this::mapStreak).collect(Collectors.toList()));
        r.setRecentDrawsSums(src.getRecentDrawsSums().stream().map(d -> {
            SumStreakResponse.RecentDrawSumResponse dr = new SumStreakResponse.RecentDrawSumResponse();
            dr.setDrawNumber(d.getDrawNumber()); dr.setDrawDate(d.getDrawDate());
            dr.setSum(d.getSum()); dr.setAboveMean(d.isAboveMean());
            return dr;
        }).collect(Collectors.toList()));
        return r;
    }

    default SumStreakResponse.DrawStreakResponse mapStreak(com.lottery.api.domain.model.DrawStreak s) {
        SumStreakResponse.DrawStreakResponse sr = new SumStreakResponse.DrawStreakResponse();
        sr.setType(s.getType()); sr.setLength(s.getLength());
        sr.setStartDate(s.getStartDate()); sr.setEndDate(s.getEndDate());
        sr.setStartDrawNumber(s.getStartDrawNumber()); sr.setEndDrawNumber(s.getEndDrawNumber());
        return sr;
    }

    default EnsemblePredictionResponse toResponse(EnsemblePrediction src) {
        if (src == null) return null;
        EnsemblePredictionResponse r = new EnsemblePredictionResponse();
        r.setLotteryType(src.getLotteryType().name());
        r.setTotalDrawsAnalyzed(src.getTotalDrawsAnalyzed());
        r.setValidationDraws(src.getValidationDraws());
        r.setModelWeights(src.getModelWeights());
        r.setValidationHitRate(src.getValidationHitRate());
        r.setMethodDescription(src.getMethodDescription());
        r.setSuggestedCombos(src.getSuggestedCombos());
        r.setScoredNumbers(src.getScoredNumbers().stream().map(n -> {
            EnsemblePredictionResponse.ScoredNumberResponse nr = new EnsemblePredictionResponse.ScoredNumberResponse();
            nr.setNumber(n.getNumber()); nr.setRank(n.getRank());
            nr.setFrequencyScore(n.getFrequencyScore()); nr.setRecencyScore(n.getRecencyScore());
            nr.setDueScore(n.getDueScore()); nr.setPairScore(n.getPairScore());
            nr.setCompositeScore(n.getCompositeScore());
            return nr;
        }).collect(Collectors.toList()));
        return r;
    }

    default CalendarFrequencyResponse toResponse(CalendarFrequency src) {
        if (src == null) return null;
        CalendarFrequencyResponse r = new CalendarFrequencyResponse();
        r.setLotteryType(src.getLotteryType().name());
        r.setTotalDraws(src.getTotalDraws());
        r.setByDayOfWeek(src.getByDayOfWeek().stream().map(d -> {
            CalendarFrequencyResponse.DayFrequencyResponse dr = new CalendarFrequencyResponse.DayFrequencyResponse();
            dr.setDayName(d.getDayName()); dr.setDayOfWeek(d.getDayOfWeek());
            dr.setDrawCount(d.getDrawCount()); dr.setNumberFrequencies(d.getNumberFrequencies());
            dr.setHotNumbers(d.getHotNumbers());
            return dr;
        }).collect(Collectors.toList()));
        r.setByMonth(src.getByMonth().stream().map(m -> {
            CalendarFrequencyResponse.MonthFrequencyResponse mr = new CalendarFrequencyResponse.MonthFrequencyResponse();
            mr.setMonthName(m.getMonthName()); mr.setMonth(m.getMonth());
            mr.setDrawCount(m.getDrawCount()); mr.setNumberFrequencies(m.getNumberFrequencies());
            mr.setHotNumbers(m.getHotNumbers());
            return mr;
        }).collect(Collectors.toList()));
        return r;
    }

    default NeuralPredictionResponse toResponse(NeuralPrediction src) {
        if (src == null) return null;
        NeuralPredictionResponse r = new NeuralPredictionResponse();
        r.setLotteryType(src.getLotteryType().name());
        r.setTotalDrawsAnalyzed(src.getTotalDrawsAnalyzed());
        r.setTrainingDraws(src.getTrainingDraws());
        r.setValidationDraws(src.getValidationDraws());
        r.setValidationHitRate(src.getValidationHitRate());
        r.setTrainingEpochs(src.getTrainingEpochs());
        r.setMethodDescription(src.getMethodDescription());
        r.setSuggestedCombos(src.getSuggestedCombos());
        r.setScoredNumbers(src.getScoredNumbers().stream().map(n -> {
            NeuralPredictionResponse.ScoredNumberResponse nr = new NeuralPredictionResponse.ScoredNumberResponse();
            nr.setNumber(n.getNumber()); nr.setRank(n.getRank());
            nr.setProbability(n.getProbability());
            nr.setRecentFreq50(n.getRecentFreq50());
            nr.setDueScore(n.getDueScore());
            nr.setTrend(n.getTrend());
            return nr;
        }).collect(Collectors.toList()));
        return r;
    }
}
