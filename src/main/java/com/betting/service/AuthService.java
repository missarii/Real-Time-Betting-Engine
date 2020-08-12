package com.betting.service;

import com.betting.dto.RegisterRequest;
import com.betting.model.User;
import com.betting.model.Wallet;
import com.betting.model.Transaction;
import com.betting.repository.UserRepository;
import com.betting.repository.WalletRepository;
import com.betting.repository.TransactionRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       WalletRepository walletRepository,
                       TransactionRepository transactionRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
