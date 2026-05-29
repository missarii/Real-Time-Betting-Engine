package com.betting.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BetRequest {

    @NotNull(message = "Event ID is required")
    private UUID eventId;

    @NotNull(message = "Odds ID is required")
    private UUID oddsId;

    @NotBlank(message = "Selection name is required")
    private String selectionName;

    @NotNull(message = "Odds value is required")
    @DecimalMin(value = "1.01", message = "Odds value must be greater than 1.00")
    private BigDecimal oddsValue;

    @NotNull(message = "Stake amount is required")
    @DecimalMin(value = "0.10", message = "Stake must be at least 0.10")
    private BigDecimal stake;
}
