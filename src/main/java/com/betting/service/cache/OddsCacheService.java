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
        this.redisTemplate = redisTemplate;
        this.oddsRepository = oddsRepository;
    }

    // Cache-Aside lookup
    public CachedOdds getOdds(UUID oddsId) {
        String key = CACHE_PREFIX + oddsId.toString();
        try {
            CachedOdds cached = (CachedOdds) redisTemplate.opsForValue().get(key);
            if (cached != null) {
                logger.debug("Odds cache HIT for key: {}", key);
                return cached;
            }
        } catch (Exception e) {
            logger.error("Error reading from Redis cache: {}", e.getMessage());
        }

        // Cache miss -> read from DB
        logger.debug("Odds cache MISS for key: {}. Loading from database...", key);
        Odds odds = oddsRepository.findById(oddsId)
                .orElseThrow(() -> new IllegalArgumentException("Odds not found for ID: " + oddsId));

        CachedOdds cachedOdds = new CachedOdds(odds);
        putOdds(oddsId, cachedOdds);
        return cachedOdds;
    }

    // Write-Through / Update cache
    public void putOdds(UUID oddsId, CachedOdds cachedOdds) {
        String key = CACHE_PREFIX + oddsId.toString();
        try {
            redisTemplate.opsForValue().set(key, cachedOdds, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            logger.debug("Odds cache PUT successful for key: {}", key);
        } catch (Exception e) {
            logger.error("Error writing to Redis cache: {}", e.getMessage());
        }
    }

    public void evictOdds(UUID oddsId) {
        String key = CACHE_PREFIX + oddsId.toString();
        try {
            redisTemplate.delete(key);
            logger.debug("Odds cache EVICT successful for key: {}", key);
        } catch (Exception e) {
            logger.error("Error evicting from Redis cache: {}", e.getMessage());
        }
    }

    @Getter
    @Setter
    public static class CachedOdds implements Serializable {
        private String id;
        private String eventId;
        private String marketName;
        private String selectionName;
        private BigDecimal oddsValue;
        private String status;

        public CachedOdds() {}

        public CachedOdds(Odds odds) {
            this.id = odds.getId().toString();
            this.eventId = odds.getEvent().getId().toString();
            this.marketName = odds.getMarketName();
            this.selectionName = odds.getSelectionName();
            this.oddsValue = odds.getOddsValue();
            this.status = odds.getStatus().name();
        }
    }
}
