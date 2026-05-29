package com.betting.repository;

import com.betting.model.Odds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OddsRepository extends JpaRepository<Odds, UUID> {
    List<Odds> findByEventId(UUID eventId);
    Optional<Odds> findByEventIdAndMarketNameAndSelectionName(UUID eventId, String marketName, String selectionName);
}
