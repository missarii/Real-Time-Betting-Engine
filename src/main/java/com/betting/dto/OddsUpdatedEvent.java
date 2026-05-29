package com.betting.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OddsUpdatedEvent {
    private UUID oddsId;
    private UUID eventId;
    private String marketName;
    private String selectionName;
    private BigDecimal oddsValue;
    private String status; // ACTIVE or SUSPENDED
}
