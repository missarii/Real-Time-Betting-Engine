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
public class BetSettledEvent {
    private UUID betId;
    private UUID userId;
    private String username;
    private UUID eventId;
    private String status; // WON or LOST
    private BigDecimal payout;
    private LocalDateTime settledAt;
}
