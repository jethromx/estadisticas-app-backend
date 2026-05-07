package com.lottery.api.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class RecentDrawSum {
    int drawNumber;
    LocalDate drawDate;
    int sum;
    boolean aboveMean;
}
