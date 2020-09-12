package com.betting.service;

import com.betting.dto.BetSettledEvent;
import com.betting.messaging.BetProducer;
import com.betting.model.Bet;
import com.betting.model.Event;
import com.betting.model.Wallet;
import com.betting.repository.BetRepository;
import com.betting.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SettlementService {

    private static final Logger logger = LoggerFactory.getLogger(SettlementService.class);

    private final EventRepository eventRepository;
    private final BetRepository betRepository;
    private final WalletService walletService;
    private final BetProducer betProducer;

    public SettlementService(EventRepository eventRepository,
                             BetRepository betRepository,
                             WalletService walletService,
                             BetProducer betProducer) {
        this.eventRepository = eventRepository;
        this.betRepository = betRepository;
        this.walletService = walletService;
        this.betProducer = betProducer;
    }

    /**
     * Settles all pending bets for a specific sports event based on the final result.
     * Result value must match one of the selection names (e.g. HOME_WIN, DRAW, AWAY_WIN).
     */
    @Transactional
    public void settleEvent(UUID eventId, String finalResult) {
        logger.info("Starting settlement for Event ID: {}, Winning selection: {}", eventId, finalResult);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (event.getStatus() == Event.EventStatus.FINISHED) {
            throw new IllegalStateException("Event has already been settled");
        }

