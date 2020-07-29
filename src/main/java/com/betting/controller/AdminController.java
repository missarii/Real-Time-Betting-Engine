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
