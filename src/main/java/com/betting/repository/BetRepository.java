package com.betting.repository;

import com.betting.model.Bet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface BetRepository extends JpaRepository<Bet, UUID> {
    List<Bet> findByUserIdOrderByPlacedAtDesc(UUID userId);
    List<Bet> findByUserUsernameOrderByPlacedAtDesc(String username);
    List<Bet> findByEventIdAndStatus(UUID eventId, Bet.BetStatus status);
}
