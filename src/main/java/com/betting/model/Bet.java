package com.betting.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bet {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", referencedColumnName = "id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "odds_id", referencedColumnName = "id", nullable = false)
    private Odds odds;

    @Column(name = "selection_name", nullable = false, length = 50)
    private String selectionName;

    @Column(name = "odds_value", nullable = false, precision = 6, scale = 2)
    private BigDecimal oddsValue;

    @Column(name = "stake", nullable = false, precision = 15, scale = 2)
    private BigDecimal stake;

    @Column(name = "potential_payout", nullable = false, precision = 15, scale = 2)
    private BigDecimal potentialPayout;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private BetStatus status = BetStatus.PENDING;

    @Column(name = "placed_at", nullable = false, updatable = false)
    private LocalDateTime placedAt;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (placedAt == null) {
            placedAt = LocalDateTime.now();
        }
    }

    public enum BetStatus {
        PENDING,
        WON,
        LOST,
        VOIDED
    }
}
