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

        // 1. Update event status to finished
        event.setStatus(Event.EventStatus.FINISHED);
        event.setEndedAt(LocalDateTime.now());
        eventRepository.save(event);

        // 2. Load all pending bets on this event
        List<Bet> pendingBets = betRepository.findByEventIdAndStatus(eventId, Bet.BetStatus.PENDING);
        logger.info("Found {} pending bet(s) to process for Event ID: {}", pendingBets.size(), eventId);

        for (Bet bet : pendingBets) {
            try {
                processBetSettlement(bet, finalResult);
            } catch (Exception e) {
                logger.error("Failed to settle Bet ID: {}. Error: {}", bet.getId(), e.getMessage());
                // In production, we would alert operations or push to a Dead Letter Queue (DLQ).
            }
        }
    }

    private void processBetSettlement(Bet bet, String finalResult) {
        boolean isWin = bet.getSelectionName().equalsIgnoreCase(finalResult);
        Wallet wallet = walletService.getWalletByUsername(bet.getUser().getUsername());

        BigDecimal payoutAmount;
        if (isWin) {
            bet.setStatus(Bet.BetStatus.WON);
            payoutAmount = bet.getPotentialPayout();
            // Credit winning amount to user's wallet
            walletService.creditWin(wallet, payoutAmount, bet.getId());
            logger.info("Bet ID: {} WON. Credited payout: {} to user: {}", bet.getId(), payoutAmount, bet.getUser().getUsername());
        } else {
            bet.setStatus(Bet.BetStatus.LOST);
            payoutAmount = BigDecimal.ZERO;
            logger.info("Bet ID: {} LOST. Stake: {} lost by user: {}", bet.getId(), bet.getStake(), bet.getUser().getUsername());
        }

        bet.setSettledAt(LocalDateTime.now());
        betRepository.save(bet);

        // 3. Publish BetSettledEvent to Kafka
        BetSettledEvent settledEvent = BetSettledEvent.builder()
                .betId(bet.getId())
                .userId(bet.getUser().getId())
                .username(bet.getUser().getUsername())
                .eventId(bet.getEvent().getId())
                .status(bet.getStatus().name())
                .payout(payoutAmount)
                .settledAt(bet.getSettledAt())
                .build();
        betProducer.sendBetSettled(settledEvent);
    }
}
