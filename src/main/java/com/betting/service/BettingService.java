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
