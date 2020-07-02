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
