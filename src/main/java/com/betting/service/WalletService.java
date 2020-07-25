package com.betting.service;

import com.betting.exception.InsufficientBalanceException;
import com.betting.model.Transaction;
import com.betting.model.Wallet;
import com.betting.repository.TransactionRepository;
import com.betting.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletService(WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    public Wallet getWalletByUsername(String username) {
        return walletRepository.findByUserUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user: " + username));
    }

    public List<Transaction> getTransactionHistory(String username) {
