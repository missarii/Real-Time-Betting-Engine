package com.betting.service;

import com.betting.dto.BetPlacedEvent;
import com.betting.dto.BetRequest;
import com.betting.messaging.BetProducer;
import com.betting.model.*;
import com.betting.repository.BetRepository;
import com.betting.repository.EventRepository;
import com.betting.repository.OddsRepository;
import com.betting.repository.UserRepository;
import com.betting.service.cache.OddsCacheService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BettingService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final OddsRepository oddsRepository;
    private final BetRepository betRepository;
    private final WalletService walletService;
    private final OddsCacheService oddsCacheService;
    private final BetProducer betProducer;

    public BettingService(UserRepository userRepository,
                          EventRepository eventRepository,
                          OddsRepository oddsRepository,
                          BetRepository betRepository,
                          WalletService walletService,
                          OddsCacheService oddsCacheService,
                          BetProducer betProducer) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.oddsRepository = oddsRepository;
        this.betRepository = betRepository;
        this.walletService = walletService;
        this.oddsCacheService = oddsCacheService;
        this.betProducer = betProducer;
    }

    public List<Bet> getBetHistory(String username) {
        return betRepository.findByUserUsernameOrderByPlacedAtDesc(username);
    }

    @Transactional
    public Bet placeBet(String username, BetRequest request) {
        // 1. Load User
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // 2. Load Event
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (event.getStatus() == Event.EventStatus.FINISHED) {
            throw new IllegalStateException("Cannot place bet on a finished event");
        }

        // 3. Fetch current live odds from cache
        OddsCacheService.CachedOdds cachedOdds = oddsCacheService.getOdds(request.getOddsId());
        
        if (!"ACTIVE".equals(cachedOdds.getStatus())) {
            throw new IllegalStateException("Betting on this market is currently suspended");
        }

        // Validate odds values are matching (anti-fraud/latency check)
        if (cachedOdds.getOddsValue().compareTo(request.getOddsValue()) != 0) {
            throw new IllegalStateException(String.format(
                    "Odds have changed from %s to %s. Please update your betslip.", 
                    request.getOddsValue(), cachedOdds.getOddsValue()
            ));
        }

        // 4. Load database reference of Odds
        Odds odds = oddsRepository.findById(request.getOddsId())
                .orElseThrow(() -> new IllegalArgumentException("Odds database entry not found"));

        // 5. Calculate potential payout
        BigDecimal potentialPayout = request.getStake().multiply(request.getOddsValue());

        // 6. Instantiate Bet
        Bet bet = Bet.builder()
                .id(UUID.randomUUID())
                .user(user)
                .event(event)
                .odds(odds)
                .selectionName(request.getSelectionName())
                .oddsValue(request.getOddsValue())
                .stake(request.getStake())
                .potentialPayout(potentialPayout)
                .status(Bet.BetStatus.PENDING)
                .placedAt(LocalDateTime.now())
                .build();

        // 7. Lock wallet funds (debits balance, saves, records transaction)
        Wallet wallet = walletService.getWalletByUsername(username);
        walletService.lockFundsForBet(wallet, request.getStake(), bet.getId());

        // 8. Save Bet
        Bet savedBet = betRepository.save(bet);

        // 9. Publish event to Kafka
        BetPlacedEvent placedEvent = BetPlacedEvent.builder()
                .betId(savedBet.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .eventId(event.getId())
                .oddsId(odds.getId())
                .selectionName(savedBet.getSelectionName())
                .oddsValue(savedBet.getOddsValue())
                .stake(savedBet.getStake())
                .placedAt(savedBet.getPlacedAt())
                .build();
        betProducer.sendBetPlaced(placedEvent);

        return savedBet;
    }
}
