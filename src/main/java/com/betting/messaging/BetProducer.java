package com.betting.messaging;

import com.betting.dto.BetPlacedEvent;
import com.betting.dto.BetSettledEvent;
import com.betting.dto.OddsUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class BetProducer {

    private static final Logger logger = LoggerFactory.getLogger(BetProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
