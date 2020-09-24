package com.betting.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "odds", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"event_id", "market_name", "selection_name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Odds {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", referencedColumnName = "id", nullable = false)
    private Event event;

    @Column(name = "market_name", nullable = false, length = 50)
    private String marketName; // e.g., "1X2", "OVER_UNDER"

    @Column(name = "selection_name", nullable = false, length = 50)
    private String selectionName; // e.g., "HOME_WIN", "DRAW", "AWAY_WIN"

    @Column(name = "odds_value", nullable = false, precision = 6, scale = 2)
    private BigDecimal oddsValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private OddsStatus status = OddsStatus.ACTIVE;

    @Version
    @Column(name = "version")
    private Integer version;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    public enum OddsStatus {
        ACTIVE,
        SUSPENDED
    }
}
