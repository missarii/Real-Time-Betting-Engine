package com.betting.service.cache;

import com.betting.model.Odds;
import com.betting.repository.OddsRepository;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
