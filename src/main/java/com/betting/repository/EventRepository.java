package com.betting.repository;

import com.betting.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findByStatus(Event.EventStatus status);
    List<Event> findBySport(String sport);
    List<Event> findByStatusNot(Event.EventStatus status);
}
