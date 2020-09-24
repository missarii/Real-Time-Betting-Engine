package com.betting.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    private UUID id;

    @Column(name = "sport", nullable = false, length = 50)
    private String sport;

    @Column(name = "home_team", nullable = false, length = 100)
    private String homeTeam;

    @Column(name = "away_team", nullable = false, length = 100)
    private String awayTeam;

    @Column(name = "home_score", nullable = false)
    @Builder.Default
    private int homeScore = 0;

    @Column(name = "away_score", nullable = false)
    @Builder.Default
    private int awayScore = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EventStatus status = EventStatus.SCHEDULED;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private java.util.List<Odds> odds = new java.util.ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    public enum EventStatus {
        SCHEDULED,
        LIVE,
        SUSPENDED,
        FINISHED
    }
}
