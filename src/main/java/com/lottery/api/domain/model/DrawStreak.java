package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class DrawStreak {
    String type;
    int length;
    LocalDate startDate;
    LocalDate endDate;
    int startDrawNumber;
    int endDrawNumber;
}
