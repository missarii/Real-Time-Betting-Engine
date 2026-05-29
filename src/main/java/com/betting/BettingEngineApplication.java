package com.betting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class BettingEngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(BettingEngineApplication.class, args);
    }
}
