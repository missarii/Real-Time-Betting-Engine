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
        return transactionRepository.findByWalletUserUsernameOrderByCreatedAtDesc(username);
    }

    @Transactional
    public void deposit(String username, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        Wallet wallet = getWalletByUsername(username);
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        Transaction tx = Transaction.builder()
                .id(UUID.randomUUID())
                .wallet(wallet)
                .type(Transaction.TransactionType.DEPOSIT)
                .amount(amount)
                .description(description != null ? description : "Deposit funds")
                .build();
        transactionRepository.save(tx);
    }

    @Transactional
    public void withdraw(String username, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        Wallet wallet = getWalletByUsername(username);

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient funds for withdrawal");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        Transaction tx = Transaction.builder()
                .id(UUID.randomUUID())
                .wallet(wallet)
                .type(Transaction.TransactionType.WITHDRAW)
                .amount(amount.negate()) // store as negative or positive based on preference, standard is positive amount, but negated is good for reporting
                .description(description != null ? description : "Withdrawal funds")
                .build();
        transactionRepository.save(tx);
    }

    // Locks wallet funds when a bet is placed
    @Transactional
    public void lockFundsForBet(Wallet wallet, BigDecimal amount, UUID betId) {
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient wallet balance to place bet");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        Transaction tx = Transaction.builder()
                .id(UUID.randomUUID())
                .wallet(wallet)
                .type(Transaction.TransactionType.BET_PLACED)
                .amount(amount.negate())
                .referenceId(betId)
                .description("Bet placement lock funds")
                .build();
        transactionRepository.save(tx);
    }

    // Credits winning payouts when bet is settled as WON
    @Transactional
    public void creditWin(Wallet wallet, BigDecimal amount, UUID betId) {
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        Transaction tx = Transaction.builder()
                .id(UUID.randomUUID())
                .wallet(wallet)
                .type(Transaction.TransactionType.BET_SETTLED)
                .amount(amount)
                .referenceId(betId)
                .description("Bet won payout credit")
                .build();
        transactionRepository.save(tx);
    }

    // Refunds staked amount if a bet is VOIDED
    @Transactional
    public void refundBet(Wallet wallet, BigDecimal amount, UUID betId) {
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        Transaction tx = Transaction.builder()
                .id(UUID.randomUUID())
                .wallet(wallet)
                .type(Transaction.TransactionType.BET_REFUNDED)
                .amount(amount)
                .referenceId(betId)
                .description("Bet voided refund")
                .build();
        transactionRepository.save(tx);
    }
}
