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
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // 1. Create and Save User
        User user = User.builder()
                .id(UUID.randomUUID())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .build();
        User savedUser = userRepository.save(user);

        // 2. Create and Save Wallet (starting balance of 1000.00)
        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .user(savedUser)
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
                .build();
        Wallet savedWallet = walletRepository.save(wallet);

        // 3. Log initial transaction
        Transaction tx = Transaction.builder()
                .id(UUID.randomUUID())
                .wallet(savedWallet)
                .type(Transaction.TransactionType.DEPOSIT)
                .amount(new BigDecimal("1000.00"))
                .description("Initial registration welcome bonus")
                .build();
        transactionRepository.save(tx);

        return savedUser;
    }
}
