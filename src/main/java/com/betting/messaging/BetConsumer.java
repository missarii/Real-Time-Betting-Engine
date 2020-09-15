package com.betting.messaging;

import com.betting.dto.BetPlacedEvent;
import com.betting.dto.BetSettledEvent;
import com.betting.dto.OddsUpdatedEvent;
import com.betting.websocket.OddsWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class BetConsumer {

    private static final Logger logger = LoggerFactory.getLogger(BetConsumer.class);

    private final OddsWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;

    public BetConsumer(OddsWebSocketHandler webSocketHandler, ObjectMapper objectMapper) {
        this.webSocketHandler = webSocketHandler;
        this.objectMapper = objectMapper;
    }

    // Each listener references its typed containerFactory so deserialization is type-safe
    @KafkaListener(topics = "odds-updates",
                   groupId = "betting-engine-group",
                   containerFactory = "oddsUpdatedContainerFactory")
    public void consumeOddsUpdated(OddsUpdatedEvent event) {
        logger.info("Consumed ODDS_UPDATED: oddsId={}, value={}", event.getOddsId(), event.getOddsValue());
        try {
            String payload = objectMapper.writeValueAsString(event);
            webSocketHandler.broadcast(payload);
        } catch (Exception e) {
            logger.error("Error broadcasting odds update: {}", e.getMessage());
        }
    }

