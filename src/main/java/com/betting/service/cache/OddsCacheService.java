package com.betting.service.cache;

import com.betting.model.Odds;
import com.betting.repository.OddsRepository;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class OddsCacheService {

    private static final Logger logger = LoggerFactory.getLogger(OddsCacheService.class);
    private static final String CACHE_PREFIX = "odds:";
    private static final long CACHE_TTL_MINUTES = 60;

    private final RedisTemplate<String, Object> redisTemplate;
    private final OddsRepository oddsRepository;

    public OddsCacheService(RedisTemplate<String, Object> redisTemplate, OddsRepository oddsRepository) {
