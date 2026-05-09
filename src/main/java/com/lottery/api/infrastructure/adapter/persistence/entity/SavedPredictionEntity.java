package com.lottery.api.infrastructure.adapter.persistence.entity;

import com.lottery.api.domain.model.LotteryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_predictions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedPredictionEntity {

    @Id
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    @Column(nullable = false)
    private String label;

    @Column(name = "saved_at", nullable = false)
    private LocalDateTime savedAt;

    @Column(name = "latest_draw_date")
    private LocalDate latestDrawDate;

    @Column(name = "combos_json", columnDefinition = "TEXT", nullable = false)
    private String combosJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "lottery_type", length = 20)
    private LotteryType lotteryType;

    @Column(name = "generation_params_json", columnDefinition = "TEXT")
    private String generationParamsJson;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "favorite", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean favorite;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
