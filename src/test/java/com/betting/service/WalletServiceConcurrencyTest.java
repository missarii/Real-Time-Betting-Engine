package com.betting.service;

import com.betting.model.User;
import com.betting.model.Wallet;
import com.betting.repository.UserRepository;
import com.betting.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class WalletServiceConcurrencyTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    private UUID userId;

    @BeforeEach
    void setUp() {
        walletRepository.deleteAll();
        userRepository.deleteAll();

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("concurrency_user")
                .email("concurrency@test.com")
                .password("password")
                .role("USER")
                .build();
        userRepository.save(user);
        userId = user.getId();

        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .user(user)
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
                .build();
        walletRepository.save(wallet);
    }

    @Test
    void testConcurrentLocksOnWallet() throws InterruptedException {
        // Retrieve the baseline wallet
        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow();
        int threadCount = 10;
