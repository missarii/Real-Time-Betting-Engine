package com.betting.controller;

import com.betting.dto.OddsUpdatedEvent;
import com.betting.messaging.BetProducer;
import com.betting.model.Event;
import com.betting.model.Odds;
import com.betting.repository.EventRepository;
import com.betting.repository.OddsRepository;
import com.betting.service.SettlementService;
import com.betting.service.cache.OddsCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final EventRepository eventRepository;
    private final OddsRepository oddsRepository;
    private final SettlementService settlementService;
    private final OddsCacheService oddsCacheService;
    private final BetProducer betProducer;

    public AdminController(EventRepository eventRepository,
                           OddsRepository oddsRepository,
                           SettlementService settlementService,
                           OddsCacheService oddsCacheService,
                           BetProducer betProducer) {
        this.eventRepository = eventRepository;
        this.oddsRepository = oddsRepository;
        this.settlementService = settlementService;
        this.oddsCacheService = oddsCacheService;
        this.betProducer = betProducer;
    }

    @GetMapping
    public String adminPanel(Model model) {
        List<Event> events = eventRepository.findAll();
        model.addAttribute("events", events);
        return "admin";
    }

    @PostMapping("/events")
    public String createEvent(@RequestParam("sport") String sport,
                              @RequestParam("homeTeam") String homeTeam,
                              @RequestParam("awayTeam") String awayTeam,
                              @RequestParam("homeOdds") BigDecimal homeOdds,
                              @RequestParam("drawOdds") BigDecimal drawOdds,
                              @RequestParam("awayOdds") BigDecimal awayOdds) {
        
        logger.info("Creating new sports event: {} vs {}", homeTeam, awayTeam);

        // 1. Create Event
        Event event = Event.builder()
                .id(UUID.randomUUID())
                .sport(sport.toUpperCase())
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .homeScore(0)
                .awayScore(0)
                .status(Event.EventStatus.SCHEDULED)
                .startTime(LocalDateTime.now().plusHours(1))
                .build();
        Event savedEvent = eventRepository.save(event);

        // 2. Build Odds Selections (1X2 Market)
        List<Odds> oddsList = new ArrayList<>();
        oddsList.add(Odds.builder().id(UUID.randomUUID()).event(savedEvent).marketName("1X2").selectionName("HOME_WIN").oddsValue(homeOdds).status(Odds.OddsStatus.ACTIVE).build());
        oddsList.add(Odds.builder().id(UUID.randomUUID()).event(savedEvent).marketName("1X2").selectionName("DRAW").oddsValue(drawOdds).status(Odds.OddsStatus.ACTIVE).build());
        oddsList.add(Odds.builder().id(UUID.randomUUID()).event(savedEvent).marketName("1X2").selectionName("AWAY_WIN").oddsValue(awayOdds).status(Odds.OddsStatus.ACTIVE).build());
        
        oddsRepository.saveAll(oddsList);

        // 3. Cache new odds in Redis
        for (Odds odds : oddsList) {
            oddsCacheService.putOdds(odds.getId(), new OddsCacheService.CachedOdds(odds));
        }

        return "redirect:/admin?eventCreated=true";
    }

    @PostMapping("/events/{id}/score")
    public String updateScore(@PathVariable("id") UUID eventId,
                              @RequestParam("homeScore") int homeScore,
                              @RequestParam("awayScore") int awayScore,
                              @RequestParam("status") String status) {
        
        logger.info("Updating status and score for event: {}", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        event.setHomeScore(homeScore);
        event.setAwayScore(awayScore);
        event.setStatus(Event.EventStatus.valueOf(status.toUpperCase()));
        eventRepository.save(event);

        return "redirect:/admin?scoreUpdated=true";
    }

    @PostMapping("/events/{id}/odds")
    public String updateOdds(@PathVariable("id") UUID eventId,
                             @RequestParam("oddsId") UUID oddsId,
                             @RequestParam("oddsValue") BigDecimal oddsValue,
                             @RequestParam("status") String status) {
        
        logger.info("Updating live odds selection {} to {}", oddsId, oddsValue);

        Odds odds = oddsRepository.findById(oddsId)
                .orElseThrow(() -> new IllegalArgumentException("Odds selection not found"));

        odds.setOddsValue(oddsValue);
        odds.setStatus(Odds.OddsStatus.valueOf(status.toUpperCase()));
        Odds savedOdds = oddsRepository.save(odds);

        // Update Redis Cache
        OddsCacheService.CachedOdds cachedRepresentation = new OddsCacheService.CachedOdds(savedOdds);
        oddsCacheService.putOdds(oddsId, cachedRepresentation);

        // Publish update event to Kafka to trigger WebSocket broadcasts
        OddsUpdatedEvent kafkaEvent = OddsUpdatedEvent.builder()
                .oddsId(savedOdds.getId())
                .eventId(eventId)
                .marketName(savedOdds.getMarketName())
                .selectionName(savedOdds.getSelectionName())
                .oddsValue(savedOdds.getOddsValue())
                .status(savedOdds.getStatus().name())
                .build();
        betProducer.sendOddsUpdated(kafkaEvent);

        return "redirect:/admin?oddsUpdated=true";
    }

    @PostMapping("/events/{id}/settle")
    public String settleEvent(@PathVariable("id") UUID eventId,
                              @RequestParam("winningResult") String winningResult) {
        
        logger.info("Triggering settlement for event {} with winning result {}", eventId, winningResult);
        
        try {
            settlementService.settleEvent(eventId, winningResult);
            return "redirect:/admin?settlementSuccess=true";
        } catch (Exception e) {
            logger.error("Settlement failed: {}", e.getMessage());
            return "redirect:/admin?error=" + e.getMessage();
        }
    }
}
