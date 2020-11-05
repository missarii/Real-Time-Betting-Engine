package com.betting.config;

import com.betting.dto.BetPlacedEvent;
import com.betting.dto.BetSettledEvent;
import com.betting.dto.OddsUpdatedEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // --- Topics ---
    @Bean
    public NewTopic betPlacementsTopic() {
        return TopicBuilder.name("bet-placements").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic betSettlementsTopic() {
        return TopicBuilder.name("bet-settlements").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic oddsUpdatesTopic() {
        return TopicBuilder.name("odds-updates").partitions(3).replicas(1).build();
    }

    // --- Typed Consumer Factories ---
    private Map<String, Object> baseConsumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "betting-engine-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return props;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OddsUpdatedEvent> oddsUpdatedContainerFactory() {
        Map<String, Object> props = baseConsumerProps();
        JsonDeserializer<OddsUpdatedEvent> deser = new JsonDeserializer<>(OddsUpdatedEvent.class, false);
        ConsumerFactory<String, OddsUpdatedEvent> cf =
                new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deser);
        ConcurrentKafkaListenerContainerFactory<String, OddsUpdatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BetPlacedEvent> betPlacedContainerFactory() {
        Map<String, Object> props = baseConsumerProps();
        JsonDeserializer<BetPlacedEvent> deser = new JsonDeserializer<>(BetPlacedEvent.class, false);
        ConsumerFactory<String, BetPlacedEvent> cf =
                new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deser);
        ConcurrentKafkaListenerContainerFactory<String, BetPlacedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BetSettledEvent> betSettledContainerFactory() {
        Map<String, Object> props = baseConsumerProps();
        JsonDeserializer<BetSettledEvent> deser = new JsonDeserializer<>(BetSettledEvent.class, false);
        ConsumerFactory<String, BetSettledEvent> cf =
                new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deser);
        ConcurrentKafkaListenerContainerFactory<String, BetSettledEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        return factory;
    }
}
