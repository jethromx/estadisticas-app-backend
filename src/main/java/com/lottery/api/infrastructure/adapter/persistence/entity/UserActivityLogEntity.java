package com.lottery.api.infrastructure.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_activity_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(nullable = false)
    private String endpoint;

    @Column(name = "http_method", length = 10, nullable = false)
    private String httpMethod;

    @Column(nullable = false)
    private String action;

    @Column(name = "lottery_type", length = 20)
    private String lotteryType;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;
}
