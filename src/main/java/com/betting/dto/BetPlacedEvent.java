package com.betting.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BetPlacedEvent {
    private UUID betId;
    private UUID userId;
    private String username;
    private UUID eventId;
    private UUID oddsId;
    private String selectionName;
    private BigDecimal oddsValue;
    private BigDecimal stake;
    private LocalDateTime placedAt;
}
