package com.betting.config;

import com.betting.dto.BetPlacedEvent;
import com.betting.dto.BetSettledEvent;
import com.betting.dto.OddsUpdatedEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
