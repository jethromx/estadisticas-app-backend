package com.lottery.api.infrastructure.adapter.persistence.entity;

import com.lottery.api.domain.model.LotteryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA que mapea la tabla {@code lottery_draws}.
 *
 * <p>Los números se almacenan en columnas separadas para permitir queries de
 * frecuencia directamente en SQL sin desempaquetar arrays.</p>
 */
@Entity
@Table(
    name = "lottery_draws",
    uniqueConstraints = @UniqueConstraint(name = "uk_lottery_draw", columnNames = {"lottery_type", "draw_number"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotteryDrawEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "lottery_type", nullable = false, length = 20)
    private LotteryType lotteryType;

    @Column(name = "draw_number", nullable = false)
    private Integer drawNumber;

    @Column(name = "draw_date", nullable = false)
    private LocalDate drawDate;

    @Column(name = "number_1", nullable = false)
    private Integer number1;

    @Column(name = "number_2", nullable = false)
    private Integer number2;

    @Column(name = "number_3", nullable = false)
    private Integer number3;

    @Column(name = "number_4", nullable = false)
    private Integer number4;

    @Column(name = "number_5", nullable = false)
    private Integer number5;

    /** Sexto número principal. NULL en sorteos con solo 5 números. */
    @Column(name = "number_6")
    private Integer number6;

    /** Séptimo número (GanaGato F7). */
    @Column(name = "number_7")
    private Integer number7;

    /** Octavo número (GanaGato F8). */
    @Column(name = "number_8")
    private Integer number8;

    /** Número adicional exclusivo de Melate (R7 del CSV). */
    @Column(name = "additional_number")
    private Integer additionalNumber;

    @Column(name = "jackpot_amount", precision = 20, scale = 2)
    private BigDecimal jackpotAmount;

    @Column(name = "first_prize_winners")
    private Integer firstPrizeWinners;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
