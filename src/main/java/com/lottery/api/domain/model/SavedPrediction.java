package com.lottery.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedPrediction {
    private String id;
    private String label;
    private LocalDateTime savedAt;
    private LocalDate latestDrawDate;
    private String combosJson;
    private LotteryType lotteryType;
    private String generationParamsJson;
    private String userId;
}
