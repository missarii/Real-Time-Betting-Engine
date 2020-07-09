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
