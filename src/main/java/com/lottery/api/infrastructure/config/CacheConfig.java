package com.lottery.api.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {

    public static final String CACHE_DUE_NUMBERS       = "analysis-due";
    public static final String CACHE_SUM_DIST          = "analysis-sum";
    public static final String CACHE_BALANCE           = "analysis-balance";
    public static final String CACHE_WINDOWED_FREQ     = "analysis-windowed";
    public static final String CACHE_PAIRS             = "analysis-pairs";
    public static final String CACHE_CHI_SQUARE        = "analysis-chi";
    public static final String CACHE_BACKTEST          = "analysis-backtest";
    public static final String CACHE_BAYESIAN          = "analysis-bayesian";
    public static final String CACHE_DRAWS             = "draws";
    public static final String CACHE_POSITION          = "analysis-position";
    public static final String CACHE_CONSECUTIVE       = "analysis-consecutive";
    public static final String CACHE_RICH_BACKTEST     = "analysis-rich-backtest";
    public static final String CACHE_TEMPORAL          = "analysis-temporal";
    public static final String CACHE_ENTROPY           = "analysis-entropy";
    public static final String CACHE_CLUSTER           = "analysis-cluster";
    public static final String CACHE_STREAK            = "analysis-streak";
    public static final String CACHE_ENSEMBLE          = "analysis-ensemble";
    public static final String CACHE_CALENDAR          = "analysis-calendar";
    public static final String CACHE_NEURAL            = "analysis-neural";

    public static final String[] ALL_CACHES = {
        CACHE_DUE_NUMBERS, CACHE_SUM_DIST, CACHE_BALANCE, CACHE_WINDOWED_FREQ,
        CACHE_PAIRS, CACHE_CHI_SQUARE, CACHE_BACKTEST, CACHE_BAYESIAN, CACHE_DRAWS,
        CACHE_POSITION, CACHE_CONSECUTIVE, CACHE_RICH_BACKTEST, CACHE_TEMPORAL,
        CACHE_ENTROPY, CACHE_CLUSTER, CACHE_STREAK, CACHE_ENSEMBLE, CACHE_CALENDAR,
        CACHE_NEURAL
    };

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(ALL_CACHES);
        manager.setCaffeine(
            Caffeine.newBuilder()
                .expireAfterWrite(6, TimeUnit.HOURS)
                .maximumSize(500)
                .recordStats()
        );
        return manager;
    }
}
