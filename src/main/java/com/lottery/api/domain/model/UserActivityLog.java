package com.lottery.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityLog {
    private Long id;
    private String userId;
    private String endpoint;
    private String httpMethod;
    private String action;
    private String lotteryType;
    private LocalDateTime timestamp;
    private String metadataJson;
}
