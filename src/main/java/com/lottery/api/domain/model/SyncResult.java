package com.lottery.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Value object con el resultado de una operación de sincronización del histórico CSV.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncResult {

    private LotteryType lotteryType;
    private int totalRecords;
    private int newRecords;
    private int updatedRecords;
    private int skippedRecords;
    private LocalDateTime syncedAt;

    /** "SUCCESS" o "ERROR". */
    private String status;

    /** Mensaje descriptivo del resultado o del error ocurrido. */
    private String message;
}
